package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Override
    public void saveWithDishFlavor(DishDto dishDto) {
        this.save(dishDto);
        Long dishId = dishDto.getId();
        List<DishFlavor> list = dishDto.getFlavors().stream().map(dishItem -> {
            dishItem.setDishId(dishId);
            return dishItem;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(list);
    }

    @Override
    public void updateWithDishFlavor(DishDto dishDto) {
        // 更新dish表
        this.updateById(dishDto);
        // 先删除dishFlavor表的旧数据，再填充新数据
        Long dishId = dishDto.getId();
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
        dishFlavorService.remove(lambdaQueryWrapper);
        // 新的口味数据
        List<DishFlavor> list = dishDto.getFlavors().stream().map(dishItem -> {
            dishItem.setDishId(dishId);
            return dishItem;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(list);
    }

    @Override
    public DishDto getWithDishFlavor(Long dishId) {
        DishDto dishDto = new DishDto();
        Dish dish = this.getById(dishId);
        BeanUtils.copyProperties(dish,dishDto);
        // 根据分类id，查出分类名称。
        Long categoryId = dish.getCategoryId();
        Category category = categoryService.getById(categoryId);
        dishDto.setCategoryName(category.getName());
        // 查出口味
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, dishId);
        List<DishFlavor> list = dishFlavorService.list(lambdaQueryWrapper);
        dishDto.setFlavors(list);
        return dishDto;
    }

}
