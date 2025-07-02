package com.sky.mapper;


import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入口味数据
     * @param
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据dish_id删除口味记录
     *
     */
    void deleteByDishId(Long dishId);
}
