package com.sky.config;


import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean
    //保证只有一个工具类
    public AliOssUtil aliOssUtil(AliOssProperties aliOssProperties){
        log.info("上传文件工具类");
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),aliOssProperties.getAccessKeySecret(), aliOssProperties.getBucketName());
    }
    //@Bean注解会在项目启动时执行生成一个对象，由spring容器接管

}
