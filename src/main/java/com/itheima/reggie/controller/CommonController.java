package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {
    @Value("${reggie.path}")
    private String basepath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String SUF = originalFilename.substring(originalFilename.lastIndexOf("."));

        String s = UUID.randomUUID().toString()+SUF;

        File die=new File(basepath);
        if(!die.exists()){
            die.mkdirs();
        }
        file.transferTo(new File(basepath + s));
        return R.success(s);
    }
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        FileInputStream fileInputStream=new FileInputStream(basepath+name);
        ServletOutputStream outputStream = response.getOutputStream();
        File file=new File(basepath+name);
//        if(!file.exists()){
//            return;
//        }
        response.setContentType("image/jpeg");
        byte [] bytes=new byte[1024];
        int len=0;
        while((len=fileInputStream.read(bytes))!=-1){
            outputStream.write(bytes,0,len);
            outputStream.flush();

        }

        fileInputStream.close();
        outputStream.close();
    }
}
