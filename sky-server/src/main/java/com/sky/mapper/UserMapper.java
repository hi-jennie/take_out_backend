package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {
    /**
     * Get user by openid
     *
     * @param openid
     * @return
     */
    @Select("SELECT * FROM user WHERE openid = #{openid}")
    User getByOpenId(String openid);

    void insert(User user);

    @Select("SELECT * FROM user WHERE id = #{userId}")
    User getById(Long userId);

    /**
     * count the user amount of specific time
     *
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
