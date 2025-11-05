package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * add item to shopping cart
     *
     * @param shoppingCartDTO
     */
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 1. check if the item is already in the cart of the specific user
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId()); // can only query the cart of the current user

        List<ShoppingCart> cartList = shoppingCartMapper.list(shoppingCart); // query the cart items based on userId and dishId/setmealId
        // 2. if exists, increase the quantity by 1 (cartList.size must either be 0 or 1)
        if (cartList != null && !cartList.isEmpty()) {
            ShoppingCart shoppingCartItem = cartList.get(0);
            shoppingCartItem.setNumber(shoppingCartItem.getNumber() + 1);
            shoppingCartMapper.updateNumberById(shoppingCartItem);
        }
        // 3. if not exists, insert a new record with quantity 1
        else {
            if (shoppingCartDTO.getDishId() != null) {
                Dish dish = dishMapper.getDishById(shoppingCartDTO.getDishId());
                shoppingCart.setName(dish.getName());
                shoppingCart.setAmount(dish.getPrice());
                shoppingCart.setImage(dish.getImage());
                if (shoppingCartDTO.getDishFlavor() != null) {
                    shoppingCart.setDishFlavor(shoppingCartDTO.getDishFlavor());
                }
            } else {
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setAmount(setmeal.getPrice());
                shoppingCart.setImage(setmeal.getImage());
            }
        }
        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        System.out.println("amount is" + shoppingCart.getAmount());
        shoppingCartMapper.insert(shoppingCart);
    }

    public List<ShoppingCart> list() {
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        return shoppingCartMapper.list(shoppingCart);
    }

    /**
     * clear shopping cart by userId
     */
    public void clear() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }
}

