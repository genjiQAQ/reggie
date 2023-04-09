package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.TreeSet;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        String password = employee.getPassword();
        String p = DigestUtils.md5DigestAsHex(password.getBytes());
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        if (emp == null || !p.equals(emp.getPassword())) {
            log.info("账号密码错误,登录失败");
            return R.error("账号密码错误,登录失败");
        }
        if (emp.getStatus() == 0) {
            log.info("账号被ban,登录失败");
            return R.error("账号被ban,登录失败");
        }
        log.info("账号登录");
        request.getSession().setAttribute("employee", emp.getId());
        return R.success(emp);


    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        log.info("退出登录");
        return R.success("退出成功");

    }

    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Employee employee) {
        log.info("新增员工{}", employee.toString());
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));


//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setCreateUser((long) request.getSession().getAttribute("employee"));
//        employee.setUpdateUser((long) request.getSession().getAttribute("employee"));
        employeeService.save(employee);
        return R.success("添加成功");

    }


    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        log.info("page={},pagesize{},name{}", page, pageSize, name);
        Page pageInfo = new Page(page, pageSize);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getName, name);
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        employeeService.page(pageInfo, queryWrapper);
        return R.success(pageInfo);

    }

    @PutMapping
    public R<String> update(@RequestBody Employee employee, HttpServletRequest request) {
        log.info(employee.toString());
//        long o = (long) request.getSession().getAttribute("employee");
//        employee.setUpdateUser(o);
//        employee.setUpdateTime(LocalDateTime.now());
        employeeService.updateById(employee);


        return R.success("修改成功");
    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable long id, HttpServletRequest request) {

        log.info("查询id{}", id);
        Employee employee = employeeService.getById(id);
        if(employee==null){
            return  R.error("wrong");
        }
        return R.success(employee);

    }


}
