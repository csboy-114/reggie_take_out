package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    @DeleteMapping("/clean")
    public R<String> clean(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(userId!=null,ShoppingCart::getUserId,userId);
        shoppingCartService.remove(lambdaQueryWrapper);
        return R.success("清除购物车成功");
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(userId!=null,ShoppingCart::getUserId,userId);
        lambdaQueryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list();
        return R.success(list);
    }

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        // 设定用户ID，指定当前是哪个用户的购物车数据
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        Long userId = BaseContext.getCurrentId();

        shoppingCart.setUserId(userId);

        // 查询当前菜品或者套餐是否在购物车中
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper = new LambdaQueryWrapper<>();

        lambdaQueryWrapper.eq(ShoppingCart::getUserId,userId);
        if(dishId!=null){
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else{
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }

        ShoppingCart one = shoppingCartService.getOne(lambdaQueryWrapper);
        // 如果已经存在，就在原来的数量上加一
        if(one==null){
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }else{
            one.setNumber(one.getNumber()+1);
            shoppingCartService.updateById(one);
        }

        // 如果不存在，添加到购物车中，数量是一

        return R.success(one);
    }

}
