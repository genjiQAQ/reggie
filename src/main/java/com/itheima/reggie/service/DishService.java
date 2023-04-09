package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.DTO.DishDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.mapper.DishMapper;

import java.util.List;

public interface DishService extends IService<Dish> {
    public void savewithF(DishDto dishDto);

    public DishDto getid(Long id);

    public void updatewf(DishDto dishDto);

    public void rmall(List<Long> ids);
}
