package com.ideaflow.yunyu.infrastructure.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类。
 * 作用：开启 Mapper 扫描，为正式业务模块提供统一的持久层接入能力。
 */
@Configuration
@MapperScan("com.ideaflow.yunyu.module")
public class MybatisPlusConfig {
}
