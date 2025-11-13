package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public OrderSubmitVO submit(OrdersDTO ordersDTO) {
        // 1. deal with exceptional cases(address and shopping != null)
        Long addressBookId = ordersDTO.getAddressBookId();
        AddressBook address = addressBookMapper.getById(addressBookId);
        if (address == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 2. insert corresponding data into orders table
        Orders currentOrder = new Orders();
        BeanUtils.copyProperties(ordersDTO, currentOrder);
        currentOrder.setPhone(address.getPhone());
        currentOrder.setAddress(address.getDetail());
        currentOrder.setConsignee(address.getConsignee());
        currentOrder.setNumber(String.valueOf(System.currentTimeMillis()));
        currentOrder.setUserId(userId);
        currentOrder.setStatus(Orders.TO_BE_CONFIRMED);
        currentOrder.setPayStatus(Orders.PAID);
        currentOrder.setDeliveryStatus(1);
        currentOrder.setOrderTime(LocalDateTime.now());
        currentOrder.setTablewareStatus(1);

        orderMapper.insert(currentOrder);

        // 3. insert corresponding data into order_details table
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(currentOrder.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        // 4. clear shopping cart data
        shoppingCartMapper.deleteByUserId(userId);

        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(currentOrder.getId())
                .orderTime(currentOrder.getOrderTime())
                .orderNumber(currentOrder.getNumber())
                .orderAmount(currentOrder.getAmount()).build();
        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "外婆下饭菜订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Transactional
    public PageResult pageQueryFromUser(OrdersPageQueryDTO ordersPageQueryDTO) {
        // setting page condition
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        // query the orders table first
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        if (page != null && !page.isEmpty()) {
            for (Orders orders : page) {
                // get the orderId so that we can find the corresponding order details
                Long orderId = orders.getId();

                // get the order details in order_detail table
                List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);

                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);
                orderVO.setOrderDetailList(orderDetailList);
                orderVOList.add(orderVO);
            }
        }
        return new PageResult(page.getTotal(), orderVOList);
    }

    /**
     * get order with details list by orderId
     *
     * @param id
     * @return
     */
    public OrderVO getWithDetailsById(Long id) {
        // get the order in orders table first
        Orders order = orderMapper.getById(id);

        // using the order id to get the order details in order_detail table
        if (order != null) {
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(order.getId());
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(order, orderVO);
            orderVO.setOrderDetailList(orderDetailList);

            return orderVO;
        } else {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
    }

    /**
     * change the status to 6 but do some validation before cancellation
     * private Integer status;
     *
     * @param id
     */
    public void userCancelById(Long id) throws Exception {
        // get the order first to check what status of this order
        Orders currOrders = orderMapper.getById(id);
        // check if the order exists
        if (currOrders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Integer currStatus = currOrders.getStatus();
        // merchant confirmed and in delivery can't be canceled.
        if (currStatus > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(currOrders.getId());
        if (currStatus.equals(Orders.TO_BE_CONFIRMED)) {
//            //invoke wechat refund api
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), // merchant order number
//                    ordersDB.getNumber(), // merchant refund number
//                    new BigDecimal(0.01),//单位 元 refund amount
//                    new BigDecimal(0.01));//original order amount
//
//            // change pay_status to REFUND
//            orders.setPayStatus(Orders.REFUND);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);


    }

    /**
     * the logic is find the order detail and convert it to shopping cart item.
     * and the user make an order again.
     *
     * @param id
     */
    @Transactional
    public void orderRepetition(Long id) {
        Long userId = BaseContext.getCurrentId();

        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);

        // convert each item in orderDetailList to ShoppingCart item. and re- inject into shoppingCart table.
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(orderDetail -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * admin search order by condition
     * (separate the functionality by extract some functionality to method;)
     *
     * @param pageQueryDTO
     * @return
     */
    @Transactional
    public PageResult conditionSearch(OrdersPageQueryDTO pageQueryDTO) {
        // search order in orders table first
        PageHelper.startPage(pageQueryDTO.getPage(), pageQueryDTO.getPageSize());

        // Select orders that meet the conditions first;
        Page<Orders> ordersPage = orderMapper.pageQuery(pageQueryDTO);
        if (ordersPage == null || ordersPage.isEmpty()) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        // we need add orderDishes  into the return item;
        List<OrderVO> orderVOList = getOrderVOList(ordersPage);

        return new PageResult(ordersPage.getTotal(), orderVOList);

    }

    /**
     * reject an order;
     *
     * @param rejectionDTO
     */
    public void reject(OrdersRejectionDTO rejectionDTO) throws Exception {
        Orders ordersDB = orderMapper.getById(rejectionDTO.getId());
        // only when the status is TO_BE_CONFIRMED, the order can be canceled✅;
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // if the payStatus is PAID, then refund first;
        if (ordersDB.getPayStatus().equals(Orders.PAID)) {
            // TODO refund;
            log.info("the admin request a refund for order(id: {})", rejectionDTO.getId());
        }

        // update corresponding infos;
        // !!! the database will indeed only update these non-null fields, and all other fields will remain unchanged ✅.
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setRejectionReason(rejectionDTO.getRejectionReason());
        orders.setStatus(Orders.CANCELLED);
        orders.setPayStatus(Orders.REFUND);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * confirm an order
     *
     * @param confirmDTO
     */
    public void confirm(OrdersConfirmDTO confirmDTO) throws Exception {
        // TODO not sure !!!
        Orders ordersDB = orderMapper.getById(confirmDTO.getId());

        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CONFIRMED);
        Integer deliverStatus = ordersDB.getDeliveryStatus();
        if (deliverStatus == 1) {
            orders.setEstimatedDeliveryTime(LocalDateTime.now().plusHours(1));
        } else {
            orders.setEstimatedDeliveryTime(ordersDB.getDeliveryTime().plusHours(1));
        }
        orderMapper.update(orders);
    }

    /**
     * cancel order
     *
     * @param cancelDTO
     */
    public void cancel(OrdersCancelDTO cancelDTO) {
        Orders ordersDB = orderMapper.getById(cancelDTO.getId());
        if (ordersDB == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        Integer orderStatus = ordersDB.getStatus();
        Orders orders = new Orders();
        if (orderStatus.equals(Orders.CONFIRMED)) {
            // TODO refund
            log.info("application for refund");
        }

        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(cancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * deliver order
     *
     * @param id
     * @throws Exception
     */
    public void deliver(Long id) throws Exception {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
    }

    /**
     * complete order(generally, In real-world business scenarios： after deliver complete delivery)
     * here we assume the order is delivered by merchant themselves and press the complete after complete delivery
     *
     * @param id
     */
    public void complete(Long id) throws Exception {
        Orders ordersDB = orderMapper.getById(id);
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> ordersPage) {
        List<OrderVO> orderVOList = new ArrayList<>();

        // iterate the ordersPage to get the id and query the order detail table to get the dishNames
        for (Orders orders : ordersPage) {
            OrderVO orderVO = new OrderVO();

            String orderDishesStr = getOrderDishesStr(orders);

            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDishes(orderDishesStr);
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    private String getOrderDishesStr(Orders orders) {
        // get order detail
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        // get the dishName and amount, then convert it to orderDish string
        List<String> dishesStrList = orderDetailList.stream().map(orderDetail -> {
            String dishName = orderDetail.getName();
            Integer amount = orderDetail.getNumber();
            return dishName + "*" + amount + ",";
        }).collect(Collectors.toList());

        return String.join(",", dishesStrList);
    }
}
