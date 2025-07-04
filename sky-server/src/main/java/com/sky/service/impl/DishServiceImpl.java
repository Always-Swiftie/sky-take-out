package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        //首先需要把DTO对象转换成一个实体Dish
        //注意DTO对象中有Flavor属性，但是Dish对象中没有
        //那么这里涉及两步操作：1.向dish表中插入1条菜品数据（不包含Flavor属性）
        //2.向Flavor表中插入若干条数据，具体取决于DTO中Flavor这个属性
        log.info("新增菜品:{}",dishDTO);
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //step1.向dish表插入1条数据
        dishMapper.insert(dish);

        //step2.获取DTO对象中的Flavor属性
        Long dishId = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && !flavors.isEmpty()){
            for(DishFlavor flavor : flavors){
                flavor.setDishId(dishId);
            }
            dishFlavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        long total = page.getTotal();
        return new PageResult(total,page);
    }

    /**
     * 批量删除菜品
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        if(ids == null || ids.isEmpty() ){
            return;
        }
        //step1.检查是否有与菜品正在起售
        for(Long id : ids){
            Dish dish = dishMapper.getById(id);
            Integer status = dish.getStatus();
            if(dish != null && Objects.equals(dish.getStatus(), StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //step2.检查是否有菜品存在关联的套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && !setmealIds.isEmpty()) {
            //当前菜品被套餐关联了，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //step3.正常删除先删除菜品，再删除口味

        dishMapper.deleteBatch(ids);
        dishFlavorMapper.deleteBatchByDishIds(ids);
    }

    /**
     * 根据id查询菜品信息
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        Dish dish = dishMapper.getById(id);
        //还需要获取当前dish对象的flavor属性的具体信息
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //参数绑定
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
    }

    /**
     * 修改菜品信息
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);
        //修改菜品口味相关信息
        //删掉原来的,重新插入新的
        dishFlavorMapper.deleteByDishId(dish.getId());
        List<DishFlavor>  dishFlavors = dishFlavorMapper.getByDishId(dish.getId());
        if(dishFlavors != null && !dishFlavors.isEmpty()){
            for(DishFlavor flavor : dishFlavors){
                flavor.setDishId(dish.getId());
            }
            dishFlavorMapper.insertBatch(dishFlavors);
        }
    }

    @Override
    @Transactional
    public void setDishStatus(Long id, Integer status) {
        Dish dish = dishMapper.getById(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询所属分类下的所有菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<DishVO> getByCategoryId(Long categoryId) {
        List<DishVO> dishVOList = dishMapper.getByCategoryId(categoryId);
        return dishVOList;
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
