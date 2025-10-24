package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "common module")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @PostMapping("/upload")
    @ApiOperation("file upload to OSS")
    public Result<String> upload(MultipartFile file) {
        log.info("file upload:{}", file);

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        // Get the extension of the file and generate a new file name using UUID
        String objectName = UUID.randomUUID() + suffix;

        // upload file to OSS, Inside AliOssUtil, we have already configured the bucket name, endpoint, accessKeyId, accessKeySecret
        try {
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            // return the file path so that the front end can access it and render the image
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("file upload failed", e);
        }
        return Result.error("file upload failed");
    }
}
