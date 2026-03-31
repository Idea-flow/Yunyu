package com.ideaflow.yunyu.infrastructure.mybatis;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类。
 * 作用：开启 Mapper 扫描，并统一注册 MyBatis-Plus 拦截器，为正式业务模块提供分页等基础持久化能力。
 */
@Configuration
@MapperScan("com.ideaflow.yunyu.module")
public class MybatisPlusConfig {

    /**
     * 注册 MyBatis-Plus 拦截器。
     * 作用：为当前项目统一开启分页插件，后续后台列表查询可直接复用 `selectPage` 实现分页能力。
     *
     * @return MyBatis-Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
