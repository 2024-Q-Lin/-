package com.sky.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 工具类
 */

/**
 * Could not autowire. No beans of 'String' type found. 的错误，
 * 实际上并不是 Spring 尝试用 @Autowired 注入基本类型，而是因为你使用了 Lombok 的 @AllArgsConstructor 注解，
 * 它自动生成了一个包含所有字段的构造函数。而 Spring 在尝试实例化这个类时，找不到合适的构造函数参数（即无法找到对应的 Bean）来进行注入。
 * <p>
 * Spring 在实例化 AliOssUtil 类时，会尝试为所有构造函数参数找到对应的 Bean。由于 String 类型的基本值（如 null）并不是 Bean，
 * Spring 无法找到对应的 Bean 来注入这些构造函数参数，因此会抛出 Could not autowire. No beans of 'String' type found. 的错误。
 */
@Data
@AllArgsConstructor
@Slf4j
//@Component
public class AliOssUtil {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    public String upload(MultipartFile file) throws IOException {

        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));

        //上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);

        //文件访问路径
        String url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;
        // 关闭ossClient
        ossClient.shutdown();
        return url;// 把上传到oss的路径返回
    }

}