package com.ideaflow.yunyu.module.system.init;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * 初始化配置属性类。
 * 作用：承接 Spring Boot 原生数据源配置，并解析出启动自动初始化所需的数据库连接参数。
 */
@ConfigurationProperties(prefix = "spring.datasource")
public class InitProperties {

    private String url = "jdbc:mysql://127.0.0.1:3306/yunyu?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    private String driverClassName = "com.mysql.cj.jdbc.Driver";
    private String username = "";
    private String password = "";

    /**
     * 判断数据库初始化配置是否已填写完整。
     *
     * @return 是否已填写完整
     */
    public boolean isConfigured() {
        return hasText(url) && hasText(username);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    /**
     * 获取数据源 URL。
     *
     * @return 数据源 URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * 设置数据源 URL。
     *
     * @param url 数据源 URL
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * 获取驱动类名。
     *
     * @return 驱动类名
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * 设置驱动类名。
     *
     * @param driverClassName 驱动类名
     */
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    /**
     * 获取数据库主机。
     *
     * @return 数据库主机
     */
    public String getHost() {
        return parseUri().getHost();
    }

    /**
     * 获取数据库端口。
     *
     * @return 数据库端口
     */
    public Integer getPort() {
        int port = parseUri().getPort();
        return port > 0 ? port : 3306;
    }

    /**
     * 获取数据库名称。
     *
     * @return 数据库名称
     */
    public String getDatabaseName() {
        String path = parseUri().getPath();
        if (path == null || path.isBlank() || "/".equals(path)) {
            return "";
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }

    /**
     * 获取数据库用户名。
     *
     * @return 数据库用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置数据库用户名。
     *
     * @param username 数据库用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取数据库密码。
     *
     * @return 数据库密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置数据库密码。
     *
     * @param password 数据库密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    private URI parseUri() {
        try {
            String jdbcBody = url.substring("jdbc:".length());
            return new URI(jdbcBody);
        } catch (URISyntaxException | StringIndexOutOfBoundsException exception) {
            throw new IllegalStateException("spring.datasource.url 配置不合法，请检查数据库连接地址", exception);
        }
    }
}
