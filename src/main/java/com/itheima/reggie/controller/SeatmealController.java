package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SeatmealController {
    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public R<List> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        lambdaQueryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        lambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> setmealList = setmealService.list(lambdaQueryWrapper);
//        List<SetmealDto> setmealDtoList = setmealList.stream().map(item -> {
//            SetmealDto setmealDto = new SetmealDto();
//            BeanUtils.copyProperties(item,setmealDto);
//            LambdaQueryWrapper<SetmealDish> l1 = new LambdaQueryWrapper<>();
//            l1.eq(SetmealDish::getSetmealId,item.getId());
//            List<SetmealDish> setmealDishList = setmealDishService.list(l1);
//            setmealDto.setSetmealDishes(setmealDishList);
//            return setmealDto;
//        }).collect(Collectors.toList());

        return R.success(setmealList);
    }

    @PostMapping("/status/{status}")
    public R<String> changeStatus(@PathVariable int status,@RequestParam List<Long> ids){
        UpdateWrapper<Setmeal> updateWrapper = new UpdateWrapper<>();
        updateWrapper.in("id",ids);
        updateWrapper.set("status",status);
        setmealService.update(updateWrapper);
        return R.success("更新套餐状态成功");
    }
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithSetmealId(ids);
        return R.success("删除套餐成功");
    }

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    @GetMapping("/page")
    public R<Page> page(String name,int page,int pageSize){
        Page<Setmeal> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Setmeal::getName,name);
        lambdaQueryWrapper.eq(Setmeal::getIsDeleted,0);
        setmealService.page(pageInfo,lambdaQueryWrapper);
        Page<SetmealDto> pageInfo1 = new Page<>(page,pageSize);
        BeanUtils.copyProperties(pageInfo,pageInfo1,"records");
        // 构造新分页的list
        List<SetmealDto> setmealDtoList = pageInfo.getRecords().stream().map(setmeal->{
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal,setmealDto);
            Category category = categoryService.getById(setmeal.getCategoryId());
            if(category!=null){
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());
        pageInfo1.setRecords(setmealDtoList);
        return R.success(pageInfo1);
    }
}
