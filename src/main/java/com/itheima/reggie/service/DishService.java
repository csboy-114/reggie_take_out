package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    public void saveWithDishFlavor(DishDto dishDto);
    public DishDto getWithDishFlavor(Long dishId);
    public void updateWithDishFlavor(DishDto dishDto);
}
