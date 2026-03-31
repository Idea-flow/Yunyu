package com.ideaflow.yunyu.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jackson 配置类。
 * 作用：提供全局可复用的 ObjectMapper Bean，供认证异常处理、统一响应输出与后续 JSON 序列化场景使用。
 */
@Configuration
public class JacksonConfig {

    /**
     * 创建全局 ObjectMapper Bean。
     *
     * @return 全局 ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper;
    }
}
