package com.sky.mapper;


import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {

    @Select("select * from sky_take_out.user where openid = #{openid}")
    User getByOpenId(String openid);

    @Select("select * from sky_take_out.user where id = #{id}")
    User getById(Long id);

    void insert(User user);

    /**
     * 根据动态条件统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
