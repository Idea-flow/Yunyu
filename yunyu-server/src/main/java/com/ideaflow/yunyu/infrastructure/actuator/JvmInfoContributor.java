package com.ideaflow.yunyu.infrastructure.actuator;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JVM 信息贡献器。
 * 作用：为 Actuator 的 info 端点补充当前运行进程的 JVM 基础信息，便于运维排查运行时环境与内存基线。
 */
@Component
public class JvmInfoContributor implements InfoContributor {

    /**
     * 向 Actuator info 端点写入 JVM 基础信息。
     *
     * @param builder Actuator 信息构建器
     */
    @Override
    public void contribute(Info.Builder builder) {
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

        Map<String, Object> jvmInfo = new LinkedHashMap<>();
        jvmInfo.put("javaVersion", System.getProperty("java.version"));
        jvmInfo.put("javaVendor", System.getProperty("java.vendor"));
        jvmInfo.put("vmName", System.getProperty("java.vm.name"));
        jvmInfo.put("vmVendor", System.getProperty("java.vm.vendor"));
        jvmInfo.put("vmVersion", System.getProperty("java.vm.version"));
        jvmInfo.put("inputArguments", runtimeMXBean.getInputArguments());
        jvmInfo.put("startTime", runtimeMXBean.getStartTime());
        jvmInfo.put("uptimeMs", runtimeMXBean.getUptime());
        jvmInfo.put("availableProcessors", runtime.availableProcessors());
        jvmInfo.put("maxMemory", runtime.maxMemory());
        jvmInfo.put("totalMemory", runtime.totalMemory());
        jvmInfo.put("freeMemory", runtime.freeMemory());
        jvmInfo.put("defaultTimeZone", ZoneId.systemDefault().getId());

        builder.withDetail("jvm", jvmInfo);
    }
}
