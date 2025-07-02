package com.sky.mapper;


import com.sky.annotation.AutoFill;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据dish_id查找套餐setmeal对象
     * 返回值是Long类型的数组，说明我们并不需要获取实际的对象，能查到id即可
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);


    void insert(SetmealDish setmealDish);

    void deleteBySetmealId(Long setmealId);

}
