package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
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

/**
 * @author zzmr
 * @create 2023-08-28 23:10
 * 通用接口
 */
@RestController
@Slf4j
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    /**
     * 参数名必须和前端传来的一致，这里是file
     *
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file);

        // 上传到阿里云服务器 第一个参数是文件的字节,第二个参数是要上传到阿里云oss的文件名
        try {
            // 先截取原文件的后缀,因为文件都是有后最的
            String originalFilename = file.getOriginalFilename();
            // 数组的最后一项才是真正的后缀,因为文件名中可能也存在点 不过看来老师不用这种方法
            // String[] split = originalFilename.split(".");
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));

            // 构建新文件名称
            String objectName = UUID.randomUUID().toString() + extension;

            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (IOException e) {
            log.error("文件上传失败:{}", e);
            e.printStackTrace();
        }

        // return Result.error("文件上传失败");
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }

}
