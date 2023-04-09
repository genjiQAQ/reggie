package com.itheima.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
@ResponseBody
@Slf4j
@ControllerAdvice(annotations = {RestController.class, Controller.class})
public class GEH {
    @ExceptionHandler(Exception.class)
    public R<String> eh(Exception e){
        log.info(e.getMessage());
        if(e.getMessage().contains("Duplicate entry")){
            String[] s = e.getMessage().split(" ");
            String m=s[s.length-4]+"yicunzai";
            return R.error(m);
        }else if(e.getMessage().contains("关联")){
            return R.error(e.getMessage());
        }
        return  R.error("未知错误");

    }


}
