package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * list shopping cart items based on conditions
     *
     * @param shoppingCart
     * @return
     */
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void update(ShoppingCart shoppingCartItem);

    /**
     * insert a single item to shopping cart
     *
     * @param shoppingCart
     */
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id=#{currentId}")
    void deleteByUserId(Long currentId);

    void insertBatch(List<ShoppingCart> shoppingCartList);
}
