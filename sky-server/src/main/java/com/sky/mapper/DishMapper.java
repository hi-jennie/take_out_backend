package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.Autofill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @Autofill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * Pagination query for dishes limit to 10 items per page
     *
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> queryPage(DishPageQueryDTO dishPageQueryDTO);

    @Select("select * from dish where id = #{id}")
    Dish getDishById(Long Id);

    /**
     * Delete dish by id, but we implement this function with delete batch which cover this single delete*
     *
     * @param id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    void deleteBatch(List<Long> ids);

    /**
     * Update dish with a new dish object
     *
     * @param dish
     */
    @Autofill(value = OperationType.UPDATE)
    void update(Dish dish);

    @Select("select * from dish where category_id = #{categoryId} and status = 1")
    List<Dish> getDishesByCategoryId(Long categoryId);
}
