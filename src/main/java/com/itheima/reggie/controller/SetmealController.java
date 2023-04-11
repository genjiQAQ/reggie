package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.DTO.DishDto;
import com.itheima.reggie.DTO.SetmealDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/setmeal")
public class SetmealController {


    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;
    @CacheEvict(value ="setmealCache",allEntries = true)
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){

        setmealService.swd(setmealDto);
        return  R.success("");

    }
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page<Setmeal> page1=new Page<>(page,pageSize);
        Page<SetmealDto> page2=new Page<>(page,pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(page1,queryWrapper);
        BeanUtils.copyProperties(page1,page2,"records");
        List<Setmeal> records=page1.getRecords();
        List<SetmealDto> list=new ArrayList<>();
        for (Setmeal record : records) {
            SetmealDto sd=new SetmealDto();
            BeanUtils.copyProperties(record,sd);
            Category category = categoryService.getById(record.getCategoryId());
            if(category!=null){
                sd.setCategoryName(category.getName());
            }
            list.add(sd);

        }

        page2.setRecords(list);



        return  R.success(page2);



    }
    @CacheEvict(value ="setmealCache",allEntries = true)
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.rwd(ids);
        return R.success("删除成功");

    }
    @CacheEvict(value ="setmealCache",allEntries = true)
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.upda(setmealDto);
        return R.success("更新成功");

    }
    @GetMapping("/{id}")
    public R<SetmealDto> getbyid(@PathVariable  Long id){
        SetmealDto sd=setmealService.getid(id);


        return R.success(sd);


    }
    @PostMapping("/status/{status}")
    public R<String> st (@PathVariable int status,@RequestParam List<Long> ids){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        List<Setmeal> list = setmealService.list(queryWrapper);
        for (Setmeal setmeal : list) {
            setmeal.setStatus(status);
        }
        setmealService.updateBatchById(list);
        return  R.success("修改状态成功");

    }

    @Cacheable(value = "setmealCache",key = "#setmeal.categoryId+'_'+#setmeal.status")
    @GetMapping("/list")
    public R<List<Setmeal>> listR( Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>() ;
        queryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus,1);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return  R.success(list);



    }
//    @PostMapping("/status")
//    public R<String> stu (){
//
//    }





}
