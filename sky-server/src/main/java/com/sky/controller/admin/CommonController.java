package com.sky.controller.admin;


import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传:{}",file);

        try {
            //获取原始文件名
            String originalFilename = file.getOriginalFilename();
            //获取原始文件名的后缀
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            //构造新文件名称
            String objectName = UUID.randomUUID().toString() + suffix;
            //获取文件的访问路径
            String filePath = aliOssUtil.upload(file.getBytes(),objectName);
            return  Result.success(filePath);
        }catch (IOException e){
            log.error("文件上传失败",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }


}
