package com.sky.controller.admin;

import com.sky.dto.OrdersConfirmDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api("admin - order related api")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * admin searches orders
     * by condition  this is similar to page query from user but return value is different.
     *
     * @param ordersPageQueryDTO
     * @return
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("Searches orders by conditions")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("admin is query order by specific condition: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    //
    @GetMapping("/details/{id}")
    @ApiOperation("Query order details")
    public Result<OrderVO> getWithDetails(@PathVariable Long id) {
        log.info("get order with corresponding details by id: {}", id);
        OrderVO orderVO = orderService.getWithDetailsById(id);
        return Result.success(orderVO);
    }

    @PutMapping("/rejection")
    @ApiOperation("reject an order")
    public Result reject(@RequestBody OrdersRejectionDTO rejectionDTO) throws Exception {
        log.info("the order of id {} is rejected by admin", rejectionDTO);
        orderService.reject(rejectionDTO);
        return Result.success();
    }

    /**
     * confirm order
     *
     * @param id
     * @return
     */
    @PutMapping("/confirm")
    @ApiOperation("admin confirm order")
    public Result confirm(@RequestBody OrdersConfirmDTO confirmDTO) {
        log.info("admin confirm the order(id: {}", confirmDTO.getId());
        orderService.confirm(confirmDTO);
        return Result.success();
    }
}
