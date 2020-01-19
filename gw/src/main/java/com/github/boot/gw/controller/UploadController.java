package com.github.boot.gw.controller;

import com.changyuan.education.commons.EducationConstants;
import com.changyuan.education.commons.exception.UpException;
import com.changyuan.education.commons.result.ResultBean;
import com.changyuan.education.gw.entity.UploadFilePath;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * 上传文件
 *
 * @author: tn
 * @Date: 2019/11/22 0022 15:41
 * @Description:
 */
@Slf4j
@RequestMapping("upload")
@RestController
public class UploadController {


    @Autowired
    private UploadFilePath uploadFilePath;


    @PostMapping("{type}")
    public ResultBean upload(HttpServletRequest request, MultipartFile file, @PathVariable("type") String type) {
        Map<String, String> uploadPathMap = uploadFilePath.getUploadPath();
        Optional<MultipartFile> multipartFile = Optional.ofNullable(file);
        if (!multipartFile.isPresent()) {
            return new ResultBean().failure(4001, "上传文件不存在");
        }
        Claims claims = (Claims) request.getAttribute(Claims.class.getSimpleName());
        String fileName = file.getOriginalFilename();
        if (!fileName.contains(".") || fileName.indexOf(".") == fileName.length()) {
            log.warn("文件格式有误");
            return new ResultBean().failure(new UpException(4001, "上传文件名称格式有误"));
        }


        String suffix = fileName.substring(fileName.lastIndexOf("."));
        String propertiesPath = uploadPathMap.get(type);
        log.info("deptNo: [{}], userNo:[{}], userId:[{}], file type:[{}], filename:[{}]  uploadPath:[{}]", claims.get(EducationConstants.RPC_DEPT_NO),
                claims.get(EducationConstants.RPC_USER_NO), claims.get(EducationConstants.RPC_USER_ID), type, fileName, propertiesPath);
        File pathFile = new File(propertiesPath);
        if (!pathFile.exists()) {
            pathFile.mkdir();
        }
        StringBuilder resultName = new StringBuilder().append(System.currentTimeMillis()).append(suffix);

        File uploadFile = new File(propertiesPath + resultName);
        try {
            file.transferTo(uploadFile);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResultBean().failure(new UpException(200, "文件上传失败"));
        }
        return new ResultBean().success("/" +type + "/" + resultName);
    }

}
