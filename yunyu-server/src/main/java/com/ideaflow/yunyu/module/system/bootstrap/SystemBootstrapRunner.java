package com.ideaflow.yunyu.module.system.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 系统启动引导器。
 * 作用：在应用启动阶段自动执行数据库引导初始化，确保数据库与核心表就绪后再进入正常运行态。
 */
@Component
public class SystemBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SystemBootstrapRunner.class);

    private final InitProperties initProperties;
    private final DatabaseBootstrapService databaseBootstrapService;
    private final SystemInitService systemInitService;

    public SystemBootstrapRunner(
            InitProperties initProperties,
            DatabaseBootstrapService databaseBootstrapService,
            SystemInitService systemInitService
    ) {
        this.initProperties = initProperties;
        this.databaseBootstrapService = databaseBootstrapService;
        this.systemInitService = systemInitService;
    }

    /**
     * 应用启动后执行数据库引导初始化。
     *
     * @param args 应用启动参数
     */
    @Override
    public void run(ApplicationArguments args) {
        log.info("开始执行 Yunyu 启动引导初始化，目标数据库：{}", initProperties.getDatabaseName());
        databaseBootstrapService.bootstrap(initProperties);
        log.info("Yunyu 启动引导初始化完成，当前系统状态：{}", systemInitService.getCurrentStatus());
        log.info("Yunyu 后端启动完成，可继续进入正常业务运行流程");
    }
}
