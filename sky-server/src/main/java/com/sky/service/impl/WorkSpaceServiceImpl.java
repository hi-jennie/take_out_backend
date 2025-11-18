package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkSpaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WorkSpaceServiceImpl implements WorkSpaceService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", 5); // completed status
        // get the turnover
        Double turnover = orderMapper.countByMap(map);
        turnover = turnover == null ? 0.0 : turnover;

        // get validOrderCount
        Integer validOrderCount = orderMapper.getOrderAmountByMap(map);
        log.info("validOrderCount: {}", validOrderCount);

        // get the total Order count to calculate the orderCompletionRate
        map.put("status", null);
        Integer totalOrderCount = orderMapper.getOrderAmountByMap(map);


        // get the newUsers
        Integer newUsers = userMapper.countByMap(map);
        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(totalOrderCount == 0 ? 0 : (validOrderCount.doubleValue() / totalOrderCount))
                .unitPrice(validOrderCount == 0 ? 0 : (turnover / validOrderCount))
                .newUsers(newUsers)
                .build();
    }

    /**
     * query order management data
     * return order count in different status
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView(LocalDateTime begin, LocalDateTime end) {
        Map map = new HashMap();
        map.put("begin", begin);
        map.put("end", end);
        map.put("status", Orders.TO_BE_CONFIRMED);
        Integer waitingOrders = orderMapper.getOrderAmountByMap(map);

        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.getOrderAmountByMap(map);

        map.put("status", Orders.COMPLETED);
        Integer completedOrders = orderMapper.getOrderAmountByMap(map);

        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.getOrderAmountByMap(map);

        map.put("status", null);
        Integer allOrders = orderMapper.getOrderAmountByMap(map);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * get dish overview of different status
     *
     * @return
     */
    public DishOverViewVO getDishOverView() {
        // get dish amount on sale
        Integer dishOnSale = dishMapper.getDishAmountByStatus(StatusConstant.ENABLE);

        // get dish not available
        Integer dishNotAvailable = dishMapper.getDishAmountByStatus(StatusConstant.DISABLE);

        return DishOverViewVO.builder()
                .sold(dishOnSale)
                .discontinued(dishNotAvailable)
                .build();
    }

    /**
     * get setmeal overview of different status
     *
     * @return
     */
    public SetmealOverViewVO getSetmealOverView() {
        // get setmeal amount on sale
        Integer setmealOnSale = setmealMapper.getSetmealAmountByStatus(StatusConstant.ENABLE);

        // get setmeal not available
        Integer setmealNotAvailable = setmealMapper.getSetmealAmountByStatus(StatusConstant.DISABLE);

        return SetmealOverViewVO.builder()
                .sold(setmealOnSale)
                .discontinued(setmealNotAvailable)
                .build();
    }
}
