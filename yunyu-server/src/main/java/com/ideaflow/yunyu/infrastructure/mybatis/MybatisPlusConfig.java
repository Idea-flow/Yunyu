package com.ideaflow.yunyu.infrastructure.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类。
 * 作用：统一声明 Mapper 扫描范围，为后续数据访问层开发提供基础配置。
 */
@Configuration
@MapperScan("com.ideaflow.yunyu.module")
public class MybatisPlusConfig {
}
