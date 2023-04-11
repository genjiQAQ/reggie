package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.DTO.DishDto;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishConntroller {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;


    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        dishService.savewithF(dishDto);
        redisTemplate.delete("dish_"+dishDto.getCategoryId()+"_"+dishDto.getStatus());
        return R.success("");

    }
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize,String name) {
        log.info("page={},pagesize{},name{}", page, pageSize);
        Page<Dish> pageInfo=new Page<>(page, pageSize);
        Page<DishDto> pageInfo1=new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo,queryWrapper);



        BeanUtils.copyProperties(pageInfo,pageInfo1,"records");
        List<Dish> records=pageInfo.getRecords();
        List<DishDto> list=new ArrayList<>();
        for (Dish record : records) {
            DishDto dd=new DishDto();
            BeanUtils.copyProperties( record,dd);
            Category byId = categoryService.getById(record.getCategoryId());
            if(byId!=null){
                dd.setCategoryName(byId.getName());
                list.add(dd);
            }

        }
        pageInfo1.setRecords(list);

        return R.success(pageInfo1);

    }
    @GetMapping("/{id}")
    public R<DishDto> getbyid(@PathVariable  Long id){
        DishDto dishDto = dishService.getid(id);
        return R.success(dishDto);


    }
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updatewf(dishDto);
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return R.success("更新成功");

    }

    @GetMapping("/list")
    public R<List<DishDto>> listR( Dish dish){
        List<DishDto> list1=null;
        String key="dish_"+dish.getCategoryId()+"_"+dish.getStatus();
        list1= (List<DishDto>) redisTemplate.opsForValue().get(key);
        if(list1!=null){
            return R.success(list1);
        }
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>() ;
        queryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        queryWrapper.eq(Dish::getStatus,1);
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        List<DishDto> list2=new ArrayList<>();
        for (Dish dish1 : list) {
            DishDto dd=new DishDto();
            BeanUtils.copyProperties(dish1,dd);
            Long categoryId = dish1.getCategoryId();
            Category byId = categoryService.getById(categoryId);
            if(byId!=null){
                dd.setCategoryName(byId.getName());
            }
            LambdaQueryWrapper<DishFlavor> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId,dd.getId());
            dd.setFlavors(dishFlavorService.list(wrapper));
            list2.add(dd);
        }
        redisTemplate.opsForValue().set(key,list2,60, TimeUnit.MINUTES);
        return  R.success(list1);

    }
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        dishService.rmall(ids);
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        return  R.success("删除成功");

    }
    @PostMapping("/status/{status}")
    public R<String> st (@PathVariable int status,@RequestParam List<Long> ids){
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId,ids);
        List<Dish> list = dishService.list(queryWrapper);
        for (Dish setmeal : list) {
            setmeal.setStatus(status);
        }
        dishService.updateBatchById(list);
        return  R.success("修改状态成功");

    }


}
