package com.ideaflow.yunyu.module.system.init;

import com.ideaflow.yunyu.module.system.init.SystemInitStatus;
import org.springframework.stereotype.Service;

/**
 * 系统初始化状态服务。
 * 作用：统一判断 Yunyu 当前是否已完成初始化，并供启动流程决定是否跳过初始化。
 */
@Service
public class SystemInitService {

    private final InitProperties initProperties;
    private final DatabaseBootstrapService databaseBootstrapService;

    public SystemInitService(InitProperties initProperties, DatabaseBootstrapService databaseBootstrapService) {
        this.initProperties = initProperties;
        this.databaseBootstrapService = databaseBootstrapService;
    }

    /**
     * 获取当前系统初始化状态。
     *
     * @return 初始化状态
     */
    public SystemInitStatus getCurrentStatus() {
        if (!initProperties.isConfigured()) {
            return SystemInitStatus.UNINITIALIZED;
        }

        if (!databaseBootstrapService.canConnectServer(initProperties)) {
            return SystemInitStatus.UNINITIALIZED;
        }

        if (databaseBootstrapService.databaseExists(initProperties)) {
            return SystemInitStatus.READY;
        }

        return SystemInitStatus.UNINITIALIZED;
    }
}
