package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.enumeration.OperationType;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //调用pageHelper执行分页查询
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        List<SetmealDish>  setmealDishes = setmealDTO.getSetmealDishes();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //对于套餐信息的插入,除了往setmeal表中插入一条数据之外，还要根据setmealDTO对象中的setmealDishes属性值操作setmeal_dish表
        setmealMapper.insert(setmeal);

        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
            setmealDishMapper.insert(setmealDish);
        });
    }

    /**
     * 根据id查询套餐信息
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {
        SetmealVO setmealVO = setmealMapper.getById(id);
        return setmealVO;
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    @Transactional
    @Override
    public void update(SetmealDTO setmealDTO) {
        //套餐的更新逻辑是,除了单独操作setmeal表中的记录行,要先全部删除掉在setmeal_dish表中与当前setmeal相关联的记录
        //然后根据新的DTO中的setmealDishes属性，逐个重新执行插入（已经完成）
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        //得到setmeal_id字段，去setmeal_dish表中删除所有字段匹配的记录
        setmealDishMapper.deleteBySetmealId(setmeal.getId());
        //删除完毕之后，再逐个重新插入
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
            setmealDishMapper.insert(setmealDish);
        });
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional
    @Override
    public void delete(List<Long> ids) {
        //首先获取到setmeal的id,去setmeal表中删除掉对应的记录行
        for(Long id:ids){
            setmealMapper.deleteById(id);
            //然后通过setmealId,把setmealDish表中关联的记录全部删除
            setmealDishMapper.deleteBySetmealId(id);
        }

    }

    /**
     * 起售or停售套餐
     * @param id
     * @param status
     */
    @Transactional
    @Override
    public void setStatus(Long id, Integer status) {
        setmealMapper.setStatus(id,status);
    }
}
