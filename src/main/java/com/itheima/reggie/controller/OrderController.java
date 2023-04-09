package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.DTO.OrdersDto;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.*;
import com.itheima.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;

    @Autowired
    private ShoppingCartService shoppingCartService;


    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("success");

    }
    @GetMapping("/userPage")
    public R<Page> pageR( int page, int pageSize){
        Page<Orders> ordersPage=new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        Long id = BaseContext.getCurrendId();
        queryWrapper.eq(Orders::getUserId,id);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(ordersPage,queryWrapper);

        Page<OrdersDto> page1=new Page<>(page,pageSize);
        BeanUtils.copyProperties(ordersPage,page1,"records");
        List<Orders> records = ordersPage.getRecords();
        List<OrdersDto> list=new ArrayList<>();
        for (Orders orders : records) {
            OrdersDto od=new OrdersDto();
            BeanUtils.copyProperties(orders,od);
            User byId = userService.getById(id);
            od.setUserName(byId.getName());
            od.setPhone(byId.getPhone());
            AddressBook byId1 = addressBookService.getById(od.getAddressBookId());
            od.setConsignee(byId1.getConsignee());
            od.setAddress(byId1.getDetail());
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId,od.getId());
            List<OrderDetail> list1 = orderDetailService.list(lambdaQueryWrapper);
            od.setOrderDetails(list1);
            list.add(od);


        }
        page1.setRecords(list);
        return R.success(page1);
    }

    @GetMapping("/page")
    public R<Page> pageR1(int page,  int pageSize,  Long number ,  LocalDateTime beginTime,LocalDateTime endTime){
        Page<Orders> ordersPage=new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(number!=null,Orders::getId,number);
        queryWrapper.between(beginTime!=null&&endTime!=null,Orders::getOrderTime,beginTime,endTime);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(ordersPage,queryWrapper);

        Page<OrdersDto> page1=new Page<>(page,pageSize);
        BeanUtils.copyProperties(ordersPage,page1,"records");
        List<Orders> records = ordersPage.getRecords();
        List<OrdersDto> list=new ArrayList<>();
        for (Orders orders : records) {
            OrdersDto od=new OrdersDto();
            BeanUtils.copyProperties(orders,od);
            User byId = userService.getById(orders.getUserId());
            od.setUserName(byId.getName());
            od.setPhone(byId.getPhone());
            AddressBook byId1 = addressBookService.getById(od.getAddressBookId());
            od.setConsignee(byId1.getConsignee());
            od.setAddress(byId1.getDetail());
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId,od.getId());
            List<OrderDetail> list1 = orderDetailService.list(lambdaQueryWrapper);
            od.setOrderDetails(list1);
            list.add(od);


        }
        page1.setRecords(list);
        return R.success(page1);
    }
    @PutMapping
    public R<String> st(@RequestBody Orders orders){
        LambdaUpdateWrapper<Orders> queryWrapper=new LambdaUpdateWrapper<>();
        queryWrapper.eq(Orders::getId,orders.getId());
        queryWrapper.set(Orders::getStatus,orders.getStatus());
        orderService.update(queryWrapper);
        return R.success("update success");
    }
    @PostMapping("/again")
    public R<String> again(@RequestBody Map<String,String> map){
        //获取order_id
        Long orderId = Long.valueOf(map.get("id"));
        //条件构造器
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        //查询订单的口味细节数据
        queryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> details = orderDetailService.list(queryWrapper);
        //获取用户id，待会需要set操作
        Long userId = BaseContext.getCurrendId();
        List<ShoppingCart> shoppingCarts = details.stream().map((item) ->{
            ShoppingCart shoppingCart = new ShoppingCart();
            //Copy对应属性值
            BeanUtils.copyProperties(item,shoppingCart);
            //设置一下userId
            shoppingCart.setUserId(userId);
            //设置一下创建时间为当前时间
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        //加入购物车
        shoppingCartService.saveBatch(shoppingCarts);
        return R.success("");
    }

}
