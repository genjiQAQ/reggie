package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.DTO.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    public void swd(SetmealDto setmealDto);

    public void rwd(List<Long> id);

    public void upda(SetmealDto setmealDto);

    public SetmealDto getid(Long id);
}
