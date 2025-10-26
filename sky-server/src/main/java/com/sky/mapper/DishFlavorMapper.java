package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {

    void insertBatch(List<DishFlavor> flavors);

    @Delete("delete from dish_flavor where dish_id = #{id}")
    void deleteByDishId(Long id);

    /**
     * @param dishIds do not use ids as the parameter name, cause it maybe be recognized as keyId by other coworkers
     *                here we use foreign key to delete not primary key
     */
    void deleteByDishIds(List<Long> dishIds);

    /**
     * get dish Flavors by dishId
     *
     * @param dishId
     * @return
     */
    @Select("select * from dish_flavor where dish_id=#{dishId}")
    List<DishFlavor> getFlavorsByDishId(Long dishId);
}
