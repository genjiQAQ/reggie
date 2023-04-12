package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.lang.invoke.LambdaMetafactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;



    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){
//            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            String code="1234";
            log.info(code);

//            SMSUtils.sendMessage("瑞吉外卖","SMS_275805208",phone,code);
            redisTemplate.opsForValue().set(phone,code,1, TimeUnit.MINUTES);

            //session.setAttribute(phone,code);
            return R.success("发送成功");

        }

        return R.error("失败");
    }

    @PostMapping("/login")
    public R<User>login(@RequestBody Map map, HttpSession session){
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        if (StringUtils.isNotEmpty(phone)){

            log.info(code);

            //Object attribute = session.getAttribute(phone);
            Object attribute = redisTemplate.opsForValue().get(phone);
            if (attribute!=null&&attribute.equals(code)){
                LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
                queryWrapper.eq(User::getPhone,phone);
                User count = userService.getOne(queryWrapper);
                if(count==null){
                    count=new User();
                    count.setPhone(phone);
                    count.setName(phone.toString());
                    userService.save(count);
                }

                session.setAttribute("user",count.getId());
                redisTemplate.delete(phone);
                return R.success(count);

            }
            return R.error("shibai");

        }

        return R.error("失败");
    }
    @PostMapping("/loginout")
    public R<String>logout(HttpSession session){
        session.removeAttribute("user");
        log.info("退出登录");
        return R.success("退出成功");
    }


}
