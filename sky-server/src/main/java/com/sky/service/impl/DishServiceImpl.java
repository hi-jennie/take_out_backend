package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    // It tells Spring that all the database operations inside this method should succeed or fail together.
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // insert 1 row into dish table
        dishMapper.insert(dish);

        // Save flavors into dish_flavor table
        // this is done by MyBatis useGeneratedKeys="true" keyProperty="id")
        Long dishId = dish.getId();

        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            // insert multiple rows into dish_flavor table
            dishFlavorMapper.insertBatch(flavors);
        }
    }


    public PageResult queryPage(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());

        Page<DishVO> page = dishMapper.queryPage(dishPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Transactional
    public void deleteBatch(List<Long> ids) {
        // dish that is on sale cannot be deleted
        for (Long id : ids) {
            Dish dish = dishMapper.getDishById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // dish that is inside a setMeal can't be deleted(setmeal_dish table is a relation table between setmeal and dish, it's many-to-many)
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // delete the dish
        // Not good practice, causes multiple database calls
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            // delete flavors associated with the dish
//            dishFlavorMapper.deleteByDishId(id);
//        }

        // Better practice, batch delete
        // sql: delete from dish where id in (?,?,?)
        dishMapper.deleteBatch(ids);
        // sql: delete from dish_flavor where dish_id in (?,?,?)
        dishFlavorMapper.deleteByDishIds(ids);
    }

    public DishVO getByIdWithFlavor(Long id) {
        // get dish by id in dish table
        Dish dish = dishMapper.getDishById(id);

        // get flavor in dish_flavor table according dish_id
        List<DishFlavor> flavors = dishFlavorMapper.getFlavorsByDishId(id);

        // encapsulate to DishVO
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    public void updateDishWithFlavor(DishDTO dishDTO) {
        // update the dish table
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // delete flavors associated with the dish by dish_id
        dishFlavorMapper.deleteByDishId(dish.getId());

        // insert new flavors into dish_flavor table
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishDTO.getId()));
            // insert multiple rows into dish_flavor table
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    @Override
    @Transactional
    public void updateDishStatus(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);

        if (status == StatusConstant.DISABLE) {
            // if dish is disabled, set the status of setmeals containing this dish to disabled
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    /**
     * get dishes by categoryId so that we can display dishes when creating a setmeal
     *
     * @param categoryId
     * @return
     */
    public List<Dish> getDishes(Long categoryId) {
        List<Dish> dishes = dishMapper.getDishesByCategoryId(categoryId);
        return dishes;
    }
}