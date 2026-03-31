package com.ideaflow.yunyu.module.system.init;

import com.ideaflow.yunyu.common.constant.ResultCode;
import com.ideaflow.yunyu.common.exception.BizException;
import com.ideaflow.yunyu.module.system.init.util.SqlScriptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 数据库引导服务。
 * 作用：在系统未初始化阶段，使用手动 JDBC 连接执行数据库连通性、建库状态和核心表检查。
 */
@Service
public class DatabaseBootstrapService {

    private static final String INIT_SCRIPT_CLASSPATH = "db/init/001-init-schema.sql";
    private static final Logger log = LoggerFactory.getLogger(DatabaseBootstrapService.class);

    /**
     * 检查数据库服务器是否可连接。
     *
     * @param properties 初始化配置
     * @return 是否可连接
     */
    public boolean canConnectServer(InitProperties properties) {
        try (Connection ignored = DriverManager.getConnection(buildServerUrl(properties), properties.getUsername(), properties.getPassword())) {
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }

    /**
     * 检查目标数据库是否存在。
     *
     * @param properties 初始化配置
     * @return 数据库是否存在
     */
    public boolean databaseExists(InitProperties properties) {
        String sql = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = ?";
        try (Connection connection = DriverManager.getConnection(buildServerUrl(properties), properties.getUsername(), properties.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, properties.getDatabaseName());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            return false;
        }
    }

    /**
     * 检查目标数据库中指定表是否存在。
     *
     * @param properties 初始化配置
     * @param tableName 表名
     * @return 表是否存在
     */
    public boolean tableExists(InitProperties properties, String tableName) {
        String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ?";
        try (Connection connection = DriverManager.getConnection(buildDatabaseUrl(properties), properties.getUsername(), properties.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, properties.getDatabaseName());
            statement.setString(2, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException exception) {
            return false;
        }
    }

    /**
     * 检查系统初始化标记是否存在。
     *
     * @param properties 初始化配置
     * @return 初始化标记是否存在
     */
    public boolean hasSystemInitFlag(InitProperties properties) {
        String sql = "SELECT COUNT(1) FROM site_config WHERE config_key = 'system.init'";
        try (Connection connection = DriverManager.getConnection(buildDatabaseUrl(properties), properties.getUsername(), properties.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        } catch (SQLException exception) {
            return false;
        }
    }

    /**
     * 检查是否已存在站长账号。
     *
     * @param properties 初始化配置
     * @return 是否已存在站长账号
     */
    public boolean hasSuperAdmin(InitProperties properties) {
        String sql = "SELECT COUNT(1) FROM user WHERE role = 'SUPER_ADMIN' AND deleted = 0";
        try (Connection connection = DriverManager.getConnection(buildDatabaseUrl(properties), properties.getUsername(), properties.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        } catch (SQLException exception) {
            return false;
        }
    }

    /**
     * 执行启动阶段数据库引导初始化。
     * 作用：在应用启动时自动检查数据库、自动建库建表，并在已初始化时自动跳过。
     *
     * @param properties 初始化配置
     */
    public void bootstrap(InitProperties properties) {
        validateProperties(properties);
        log.info("开始检查数据库服务器连接，目标地址：{}:{}", properties.getHost(), properties.getPort());

        if (!canConnectServer(properties)) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "数据库服务器不可连接，请检查 yunyu.init 配置");
        }
        log.info("数据库服务器连接成功");

        boolean databaseExists = databaseExists(properties);
        log.info("目标数据库 `{}` 是否已存在：{}", properties.getDatabaseName(), databaseExists ? "是" : "否");
        if (!databaseExists) {
            createDatabaseIfNotExists(properties);
            log.info("已自动创建数据库 `{}`", properties.getDatabaseName());
        } else {
            log.info("跳过数据库创建，数据库 `{}` 已存在", properties.getDatabaseName());
        }

        boolean coreTablesReady = hasCoreTables(properties);
        log.info("核心表是否已存在：{}", coreTablesReady ? "是" : "否");
        if (!coreTablesReady) {
            executeInitScript(properties);
            log.info("已执行初始化 SQL，核心表创建完成");
        } else {
            log.info("跳过建表逻辑，核心表已存在");
        }

        boolean initFlagExists = hasSystemInitFlag(properties);
        log.info("初始化完成标记是否存在：{}", initFlagExists ? "是" : "否");
        if (!initFlagExists) {
            upsertSystemInitFlag(properties);
            log.info("已写入系统初始化完成标记 `system.init`");
        } else {
            log.info("跳过初始化标记写入，`system.init` 已存在");
        }
    }

    private void validateProperties(InitProperties properties) {
        if (!properties.isConfigured()) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "数据库初始化配置不完整，请检查 yunyu.init 配置");
        }
    }

    private boolean hasCoreTables(InitProperties properties) {
        return tableExists(properties, "user")
                && tableExists(properties, "post")
                && tableExists(properties, "post_content")
                && tableExists(properties, "site_config");
    }

    private void createDatabaseIfNotExists(InitProperties properties) {
        String sql = "CREATE DATABASE IF NOT EXISTS `" + properties.getDatabaseName() + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci";
        try (Connection connection = DriverManager.getConnection(buildServerUrl(properties), properties.getUsername(), properties.getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "创建数据库失败");
        }
    }

    private void executeInitScript(InitProperties properties) {
        Resource resource = new ClassPathResource(INIT_SCRIPT_CLASSPATH);
        try {
            String script = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            List<String> statements = SqlScriptUtils.splitStatements(script);
            try (Connection connection = DriverManager.getConnection(buildDatabaseUrl(properties), properties.getUsername(), properties.getPassword());
                 Statement statement = connection.createStatement()) {
                for (String sql : statements) {
                    statement.execute(sql);
                }
            }
        } catch (IOException exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "读取初始化 SQL 脚本失败");
        } catch (SQLException exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "执行初始化 SQL 失败");
        }
    }

    private void upsertSystemInitFlag(InitProperties properties) {
        String sql = """
                INSERT INTO site_config (config_key, config_name, config_json, remark, created_time, updated_time)
                VALUES ('system.init', '系统初始化标记',
                JSON_OBJECT('initialized', true, 'initializedTime', ?, 'version', 'v0.0.1'),
                '系统启动自动初始化完成标记', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE
                config_name = VALUES(config_name),
                config_json = VALUES(config_json),
                remark = VALUES(remark),
                updated_time = CURRENT_TIMESTAMP
                """;
        try (Connection connection = DriverManager.getConnection(buildDatabaseUrl(properties), properties.getUsername(), properties.getPassword());
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, LocalDateTime.now().toString());
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new BizException(ResultCode.INTERNAL_SERVER_ERROR, "写入初始化完成标记失败");
        }
    }

    private String buildServerUrl(InitProperties properties) {
        return "jdbc:mysql://" + properties.getHost() + ":" + properties.getPort()
                + "/?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    }

    private String buildDatabaseUrl(InitProperties properties) {
        return "jdbc:mysql://" + properties.getHost() + ":" + properties.getPort()
                + "/" + properties.getDatabaseName()
                + "?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai";
    }
}
