package com.ideaflow.yunyu.infrastructure.mybatis;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 数据源引导配置类。
 * 作用：在正式数据源创建前补齐 MySQL 自动建库参数，兼容“启动自动建库”和 MyBatis-Plus 同时启用的场景。
 */
@Configuration
public class DataSourceBootstrapConfig {

    /**
     * 创建主数据源。
     *
     * @param dataSourceProperties Spring Boot 原生数据源配置
     * @return 主数据源
     */
    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        dataSource.setJdbcUrl(appendCreateDatabaseIfNeed(dataSourceProperties.getUrl()));
        dataSource.setUsername(dataSourceProperties.getUsername());
        dataSource.setPassword(dataSourceProperties.getPassword());
        return dataSource;
    }

    /**
     * 为 MySQL JDBC URL 补充自动建库参数。
     *
     * @param jdbcUrl 原始 JDBC URL
     * @return 补充参数后的 JDBC URL
     */
    private String appendCreateDatabaseIfNeed(String jdbcUrl) {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return jdbcUrl;
        }
        if (!jdbcUrl.startsWith("jdbc:mysql:")) {
            return jdbcUrl;
        }
        if (jdbcUrl.contains("createDatabaseIfNotExist=")) {
            return jdbcUrl;
        }
        return jdbcUrl.contains("?")
                ? jdbcUrl + "&createDatabaseIfNotExist=true"
                : jdbcUrl + "?createDatabaseIfNotExist=true";
    }
}
