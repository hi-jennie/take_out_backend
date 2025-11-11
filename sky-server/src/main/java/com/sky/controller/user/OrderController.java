package com.sky.controller.user;


import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Api("orders related api")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * submit a new order
     *
     * @param ordersDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("submit a new order")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersDTO ordersDTO) {
        log.info("submit an order,{}", ordersDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersDTO);
        return Result.success(orderSubmitVO);
    }

    /**
     * order payment
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("order payment")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("order payment：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("generate prepaid token：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    /**
     * query historical orders
     */
    @GetMapping("/historyOrders")
    @ApiOperation("query historical orders")
    public Result<PageResult> getHistoryOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("this user is querying the history orders page: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.pageQueryFromUser(ordersPageQueryDTO);
        return Result.success(pageResult);

    }

    /**
     * get order with all the detail list by order id
     *
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("get order with detail list by order id")
    public Result<OrderVO> getWithDetailsById(@PathVariable Long id) {
        log.info("get order with detail list of orderId: {} ", id);
        OrderVO orderVO = orderService.getWithDetailsById(id);
        return Result.success(orderVO);
    }

    /**
     * not delete this order from order table, but change the order status
     *
     * @param id
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("cancel order by id")
    public Result cancel(@PathVariable Long id) throws Exception {
        log.info("cancel order of id : {}", id);
        orderService.userCancelById(id);
        return Result.success();
    }
}
