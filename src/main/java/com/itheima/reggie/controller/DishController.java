package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/list")
    public R<List<DishDto>> list(DishDto dishDto){

        List<DishDto> dishDtoList = null;

        String key = "dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus();

        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);

        if(dishDtoList!=null){
            return R.success(dishDtoList);
        }

        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dishDto.getCategoryId()!=null,Dish::getCategoryId,dishDto.getCategoryId());
        List<Dish> list = dishService.list(lambdaQueryWrapper);
        dishDtoList = list.stream().map(dish -> {
            DishDto dto = new DishDto();
            BeanUtils.copyProperties(dish,dto);
            // 设置分类名
            Category category = categoryService.getById(dish.getCategoryId());
            if(category!=null){
                dishDto.setCategoryName(category.getName());
            }
            // 设置口味
            LambdaQueryWrapper<DishFlavor> dishDtoLambdaQueryWrapper = new LambdaQueryWrapper<>();
            dishDtoLambdaQueryWrapper.eq(DishFlavor::getDishId,dish.getId());
            List<DishFlavor> flavorList = dishFlavorService.list(dishDtoLambdaQueryWrapper);
            dto.setFlavors(flavorList);
            return dto;
        }).collect(Collectors.toList());

        // 存入 redis 中
        redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }

    @GetMapping("/page")
    public R<Page> page(String name,int page,int pageSize){
        Page<Dish> pg = new Page<>(page,pageSize);
        LambdaQueryWrapper<Dish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Dish::getName,name);
        lambdaQueryWrapper.eq(Dish::getIsDeleted,0); // 查出未被删除的数据
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pg, lambdaQueryWrapper);
        Page<DishDto> dtoPage = new Page<>(page,pageSize);
        BeanUtils.copyProperties(pg,dtoPage,"records");
        List<DishDto> dishDtoList = pg.getRecords().stream().map(dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            Category category = categoryService.getById(dish.getCategoryId());
            dishDto.setCategoryName(category.getName());
            return dishDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(dishDtoList);
        return R.success(dtoPage);
    }
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithDishFlavor(dishDto);
        String key = "dish_"+dishDto.getCategoryId()+"_1";

        redisTemplate.delete(key);
        return R.success("新增菜品成功");
    }
    @GetMapping("/{dishId}")
    public R<DishDto> getDishWithFlavor(@PathVariable Long dishId){
        DishDto dishDto = dishService.getWithDishFlavor(dishId);
        return R.success(dishDto);
    }
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithDishFlavor(dishDto);
        String key = "dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("更新菜品成功");
    }
    @PostMapping("/status/{status}")
    public R<String> changeStatus(@PathVariable int status,@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        log.info("status:{}",status);
        // update dish set status = {status} where id in  ids (,,,)
        UpdateWrapper<Dish> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id",ids);
        updateWrapper.set("status",status);
        dishService.update(updateWrapper);
        String resStr = "";
        if(status == 1){
            resStr = "启售成功";
        }else {
            resStr = "停售成功";
        }
        return R.success(resStr);
    }
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        UpdateWrapper<Dish> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id",ids);
        updateWrapper.set("is_deleted",1);
        dishService.update(updateWrapper);
        return R.success("删除成功");
    }
}
