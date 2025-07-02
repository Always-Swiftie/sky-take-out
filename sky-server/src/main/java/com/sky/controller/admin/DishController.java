package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;



    /**
     * 新增菜品
     * @param dishDTO
     *
     */
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){
        log.info("新增菜品:{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 菜品分页查询
     *
     */
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        log.info("菜品分页查询:{}",dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }
    /**
     * 批量删除菜品
     *
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        //获取到请求路径中的ids参数
        log.info("批量删除菜品:{}",ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 根据id查询菜品
     */
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){
        log.info("根据id查询菜品:{}",id);
        DishVO dish = dishService.getById(id);
        return Result.success(dish);
    }

    /**
     * 修改菜品信息
     *
     */
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品信息:{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    /**
     * 修改菜品起停售状态
     *
     */
    @PostMapping("/status/{status}")
    public Result setDishStatus(@PathVariable Integer status,Long id){
        log.info("修改菜品售卖状态:{}",id);
        dishService.setDishStatus(id,status);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品列表
     */
    @GetMapping("/list")
    public Result getDishByCategoryId(@RequestParam Long categoryId){
        log.info("根据分类id查询菜品列表:{}",categoryId);
        List<DishVO> dishes = dishService.getByCategoryId(categoryId);
        return Result.success(dishes);
    }


}
