package com.ideaflow.yunyu.nativeimage.support.core.config;

import com.ideaflow.yunyu.nativeimage.support.core.aot.YunyuNativeRuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

/**
 * Yunyu Native 支撑基础配置类。
 * 作用：向 Spring AOT 流程注册 Yunyu Native 支撑模块的基础 RuntimeHints。
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(YunyuNativeRuntimeHintsRegistrar.class)
public class YunyuNativeSupportConfiguration {
}
