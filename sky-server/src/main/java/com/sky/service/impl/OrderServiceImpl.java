package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

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
        currentOrder.setStatus(Orders.PENDING_PAYMENT);
        currentOrder.setPayStatus(Orders.UN_PAID);
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
}
