package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        // 先往套餐表添加数据
        this.save(setmealDto);
        Long setmealId = setmealDto.getId();
        List<SetmealDish> list = setmealDto.getSetmealDishes().stream().map(setmealDish->{
            setmealDish.setSetmealId(setmealId);
            return setmealDish;
        }).collect(Collectors.toList());
        // 往套餐菜品关联表添加数据
        setmealDishService.saveBatch(list,3000);
    }

    @Override
    public void removeWithSetmealId(List<Long> ids) {
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(Setmeal::getStatus,1);
        lambdaQueryWrapper.in(Setmeal::getId,ids);
        if(this.count(lambdaQueryWrapper)>0){
            throw new CustomException("不能删除启售状态的套餐");
        }
        UpdateWrapper<Setmeal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id",ids);
        updateWrapper.set("is_deleted",1);
        this.update(updateWrapper);
        // 删除对应套餐中的菜品数据
        UpdateWrapper<SetmealDish> updateWrapper1 = new UpdateWrapper<>();
        updateWrapper1.in("setmeal_id",ids);
        updateWrapper1.set("is_deleted",1);
        setmealDishService.update(updateWrapper1);
    }
}
