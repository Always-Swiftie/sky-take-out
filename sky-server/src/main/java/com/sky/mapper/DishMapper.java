package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据dish_id查询菜品信息
     *
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 根据dish_id删除dish数据
     *
     */
    void deleteById(Long id);

    /**
     * 批量删除
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改菜品数据
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类id获取菜品
     */
    List<DishVO> getByCategoryId(Long categoryId);


    /**
     * 动态条件查询菜品
     * @param dish
     * @return
     */
    List<Dish> list(Dish dish);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);


}
