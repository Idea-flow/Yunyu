package com.ideaflow.yunyu.nativeimage.support.core.aot;

import com.ideaflow.yunyu.nativeimage.support.core.config.YunyuNativeSupportSettings;
import org.graalvm.nativeimage.hosted.Feature;

/**
 * Yunyu Native Runtime Feature 类。
 * 作用：在 GraalVM Native Image 分析阶段补充 Spring AOT 之外仍然需要的 MyBatis-Plus Native 运行期元数据。
 */
public class YunyuNativeRuntimeFeature implements Feature {

    /**
     * 在 Native Image 分析前注册 MyBatis-Plus Native 运行所需的关键元数据。
     *
     * @param access GraalVM 提供的分析前访问入口
     */
    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        NativeImageClassScanner scanner = new NativeImageClassScanner(access.getApplicationClassLoader());
        YunyuNativeSupportSettings settings =
                YunyuNativeSupportSettings.fromSystemProperty(access.getApplicationClassLoader());
        YunyuNativeSupportModule module = new YunyuNativeSupportModule(scanner, settings);
        module.registerAll();
    }
}
