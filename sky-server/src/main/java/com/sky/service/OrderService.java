package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {
    OrderSubmitVO submit(OrdersDTO ordersDTO);


    /**
     * order payment
     *
     * @param ordersPaymentDTO
     * @return
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * payment success, modify order status
     *
     * @param outTradeNo
     */
    void paySuccess(String outTradeNo);

    PageResult pageQueryFromUser(OrdersPageQueryDTO ordersPageQueryDTO);

    OrderVO getWithDetailsById(Long id);

    void userCancelById(Long id) throws Exception;

    void orderRepetition(Long id);

    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    void reject(OrdersRejectionDTO rejectionDTO) throws Exception;

    void confirm(OrdersConfirmDTO confirmDTO) throws Exception;

    void cancel(OrdersCancelDTO cancelDTO) throws Exception;

    void deliver(Long id) throws Exception;

    void complete(Long id) throws Exception;

    OrderStatisticsVO statistics();

    void remind(Long id);
    
}
