package com.ideaflow.yunyu.module.system.init;

/**
 * 系统初始化状态枚举。
 * 作用：统一描述 Yunyu 后端当前所处的初始化阶段，便于启动判断和前端引导。
 */
public enum SystemInitStatus {

    UNINITIALIZED,
    PARTIALLY_INITIALIZED,
    READY
}
