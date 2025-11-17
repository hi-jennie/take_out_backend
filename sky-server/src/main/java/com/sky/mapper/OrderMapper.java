package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    void insert(Orders currentOrder);

    /**
     * get order information by order number
     *
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * change order information
     *
     * @param orders
     */
    void update(Orders orders);

    /**
     * return a list of condition
     *
     * @param ordersPageQueryDTO is used encapsulating query conditions
     * @return
     */
    Page<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * get order by id
     *
     * @param id
     * @return
     */
    @Select("select * from orders where id=#{id}")
    Orders getById(Long id);


    /**
     * get statistics of different status
     *
     * @param status
     * @return
     */
    @Select("select count(id) from orders where status=#{status}")
    Integer getAmountByStatus(Integer status);

    /**
     * select orders that is pending_payment status and place the order 15 minute before.
     *
     * @param status
     * @param orderTime
     * @return
     */
    @Select("select * from orders where status=#{status} and order_time < #{orderTime}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime orderTime);

    Double countByMap(Map map);

    /**
     * get the order amount of specific time and status
     * query condition are encapsulated in map
     *
     * @param map
     * @return
     */
    Integer getOrderAmountByMap(Map map);

    /**
     * Get the top 10 best-selling products
     *
     * @param begin
     * @param end
     * @return
     */
    List<GoodsSalesDTO> getTop10(LocalDateTime begin, LocalDateTime end);
}
