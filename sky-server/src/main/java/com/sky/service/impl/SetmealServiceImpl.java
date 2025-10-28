package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    public void saveWithDish(SetmealDTO setmealDTO) {
        // save setmeal in setmeal table
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        // get the meal id after insertion
        Long setmealId = setmeal.getId();

        // set setmealId for each dish in setmealDishes
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealId);
        }

        // save setmealDishes in setmeal_dish table
        setmealDishMapper.batchInsert(setmealDishes);
    }

    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        // using PageHelper for pagination
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        // the first query to get paginated setmeals
        Page<SetmealVO> setmeals = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(setmeals.getTotal(), setmeals.getResult());
    }

    /**
     * delete dishes in batch
     *
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // check if the setmeal is on sale before deletion
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if (StatusConstant.ENABLE == setmeal.getStatus()) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        // delete setmeal in setmeal table
        setmealMapper.deleteByIds(ids);

        // delete dish in setmeal_dish table according setmeal id
        setmealDishMapper.deleteByIds(ids);
    }
}
