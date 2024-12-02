package com.wenqi.springboot.controller.stream;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;

/**
 * 设想使用流式上传文件, 减少内存占用, 现在 demo 还是有 oom 现象, 待解决
 * @author liangwenqi
 * @date 2024/12/2
 */
@RestController
@RequestMapping("stream")
public class StreamController {
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamController.class);

    private static final String TEMP_PATH = "C://temp//";
    private static final String TEMP_FILE = TEMP_PATH + "test.txt";

    static {
        // 文件大小（以字节为单位）
        long fileSize = 100 * 1024 * 1024; // 100MB

        // 创建文件对象
        File file = new File(TEMP_FILE);

        if (!file.exists()) {
            // 获取父目录
            File parentDir = file.getParentFile();

            // 如果父目录不存在，则创建它
            if (parentDir != null && !parentDir.exists()) {
                boolean created = parentDir.mkdirs();
                if (!created) {
                    LOGGER.error("无法创建父目录");
                }
            }

            // 写入文件
            try (BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(file.toPath()))) {
                // 创建一个 100MB 的空字节数组
                byte[] buffer = new byte[1024]; // 1KB 的缓冲区
                long remaining = fileSize;

                while (remaining > 0) {
                    int toWrite = (int) Math.min(buffer.length, remaining);
                    bos.write(buffer, 0, toWrite);
                    remaining -= toWrite;
                }

               LOGGER.info("文件已成功创建并填充到 100MB。");
            } catch (IOException e) {
                LOGGER.error("无法写入文件", e);
            }
        }

    }


    @PostMapping(value = "/postRequestNotStream")
    Integer postRequestNotStream() {
        // 要上传的文件
        File file = new File(TEMP_FILE); // 请根据需要修改文件路径
        // 目标 URL
        String url = "http://localhost:28080/stream/uploadSave";
        // 创建 RestTemplate
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

        // 创建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 创建文件资源
        FileSystemResource fileResource = new FileSystemResource(file);

        // 创建 MultiValueMap 来封装文件
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource); // "file" 是服务器端接收文件的字段名

        // 创建请求实体
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);

        // 发送 POST 请求
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        LOGGER.info("上传结果: {}", JSON.toJSONString(response));
        return 1;
    }

    @PostMapping(value = "/postRequestUseStream")
    Integer postRequestUseStream(Reader reader) {
        // 要上传的文件
        File file = new File(TEMP_FILE); // 请根据需要修改文件路径

        // 目标 URL
        String url = "http://localhost:28080/stream/uploadSave";

        // 创建 RestTemplate
        RestTemplate restTemplate = new RestTemplate(new HttpComponentsClientHttpRequestFactory());

        // 创建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 创建 MultiValueMap 来封装文件
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        Resource fileSystemResource = new FileSystemResource(file);
        body.add("file", fileSystemResource); // "file" 是服务器端接收文件的字段名

        // 创建请求实体
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 发送 POST 请求
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

        // 打印响应
        LOGGER.info("上传结果: {}", JSON.toJSONString(response));
        return 1;
    }


    @PostMapping(value = "/uploadSave")
    Integer uploadFile(@RequestPart(value = "file") MultipartFile file) {
        // 生成保存文件的路径
        File saveFile = new File(TEMP_PATH + System.currentTimeMillis() + "-" + file.getOriginalFilename());

        // 使用流式写入文件
        try (InputStream inputStream = file.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(saveFile)) {

            byte[] buffer = new byte[1024]; // 1KB 的缓冲区
            int bytesRead;

            // 读取输入流并写入到输出流
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

        } catch (IOException e) {
            LOGGER.error("文件上传失败", e);
            return 0; // 返回失败状态
        }

        return 1; // 返回成功状态
    }

}
