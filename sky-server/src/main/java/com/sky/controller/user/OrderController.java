package com.sky.controller.user;


import com.sky.dto.OrdersDTO;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
