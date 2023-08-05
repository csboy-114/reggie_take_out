package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(@RequestBody Employee employee, HttpServletRequest request){
        log.info(employee.toString());
        String username = employee.getUsername();
        String password = employee.getPassword();
        // 判断用户名是否存在于数据库中，如果没有，登录失败，如果用户名正确，则再比较密码
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(StringUtils.isNotEmpty(username),Employee::getUsername,username);
        Employee emp = employeeService.getOne(lambdaQueryWrapper);
        if(emp!=null){
            // 数据库中有此用户
            if(StringUtils.equals(emp.getPassword(),DigestUtils.md5DigestAsHex(password.getBytes()))){
                // 密码对比成功
                if(emp.getStatus()==1){
                    // 员工未被禁用
                    request.getSession().setAttribute("employee",emp.getId());
                    return  R.success(emp);
                }else{
                    return R.error("员工已禁用");
                }
            }else {
                return R.error("密码错误");
            }
        }
        return R.error("登录失败");
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        request.getSession().removeAttribute("employee");
        return R.success("退出登录成功");
    }

    @PostMapping
    public R<String> save(@RequestBody Employee employee,HttpServletRequest request){
        log.info("新增员工：{}",employee);
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employeeService.save(employee);
        return R.success("新增员工成功");
    }
    @GetMapping("/page")
    public R<Page> page(String name,int page,int pageSize){
        Page<Employee> pg = new Page<>(page,pageSize);
        LambdaQueryWrapper<Employee> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pg,lambdaQueryWrapper);
        return R.success(pg);
    }
    @PutMapping
    public R<String> update(HttpServletRequest request,@RequestBody Employee employee){
        log.info("线程id：{}",Thread.currentThread().getId());
        employeeService.updateById(employee);
        return R.success("更改状态成功");
    }
    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("接收到id：{}",id);
        Employee emp = employeeService.getById(id);
        return R.success(emp);
    }
}
