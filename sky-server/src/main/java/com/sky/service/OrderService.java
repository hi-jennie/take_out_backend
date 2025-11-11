package com.sky.service;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
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
}
