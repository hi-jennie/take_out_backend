package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
/**
 * OSS configuration class used for setting up Object Storage Service parameters
 */
public class OssConfiguration {

    // generate AliOssUtil bean so that it can be injected elsewhere
    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil ossProperties(AliOssProperties ossProperties) {
        log.info("OssProperties bean is being created");
        return new AliOssUtil(ossProperties.getEndpoint(),
                ossProperties.getAccessKeyId(),
                ossProperties.getAccessKeySecret(),
                ossProperties.getBucketName());
    }
}
