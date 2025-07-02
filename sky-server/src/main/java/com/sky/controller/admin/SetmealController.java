package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    SetmealService setmealService;

    /**
     * 套餐分页查询
     */
    @GetMapping("/page")
    public Result page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询:{}", setmealPageQueryDTO);
        PageResult page = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 新增套餐
     */
    @PostMapping
    public Result save(@RequestBody SetmealDTO setmealDTO){
        log.info("新增菜品:{}", setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();
    }

    /**
     * 根据id获取套餐
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result getById(@PathVariable Long id){
        log.info("根据id查询套餐:{}", id);
        SetmealVO setmeal = setmealService.getById(id);
        return Result.success(setmeal);
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     * @return
     */
    @PutMapping
    public Result updateSetmeal(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐信息:{}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    /**
     * 批量删除套餐信息
     * @param ids
     * @return
     */
    @DeleteMapping
    public Result delete(@RequestParam List<Long> ids){
        log.info("批量删除套餐:{}", ids);
        setmealService.delete(ids);
        return Result.success();
    }

    /**
     * 起售or停售菜品
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    public Result setmealStatus(@PathVariable Integer status,Long id){
        log.info("套餐起售，停售:{}", status);
        setmealService.setStatus(id,status);
        return Result.success();
    }


}
