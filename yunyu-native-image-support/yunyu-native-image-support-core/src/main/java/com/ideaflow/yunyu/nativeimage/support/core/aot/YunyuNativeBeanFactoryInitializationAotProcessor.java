package com.ideaflow.yunyu.nativeimage.support.core.aot;

import com.ideaflow.yunyu.nativeimage.support.core.config.YunyuNativeSupportSettings;
import org.springframework.aot.generate.GenerationContext;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotContribution;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.aot.BeanFactoryInitializationCode;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Yunyu Native BeanFactory AOT 处理器类。
 * 作用：在 Spring AOT 阶段读取业务主包与扩展包配置，并将业务相关 RuntimeHints 动态补充进生成上下文。
 */
public class YunyuNativeBeanFactoryInitializationAotProcessor implements BeanFactoryInitializationAotProcessor {

    /**
     * 在 AOT 阶段处理当前 BeanFactory。
     *
     * @param beanFactory 当前 Spring BeanFactory
     * @return AOT 贡献对象
     */
    @Override
    public BeanFactoryInitializationAotContribution processAheadOfTime(ConfigurableListableBeanFactory beanFactory) {
        YunyuNativeSupportSettings settings = YunyuNativeSupportSettings.fromBeanFactory(beanFactory);
        ClassLoader classLoader = beanFactory.getBeanClassLoader();
        return new YunyuNativeBeanFactoryInitializationAotContribution(settings, classLoader);
    }

    /**
     * Yunyu Native BeanFactory AOT 贡献实现类。
     * 作用：将业务扫描得到的 RuntimeHints 写入当前生成上下文。
     */
    private static final class YunyuNativeBeanFactoryInitializationAotContribution
            implements BeanFactoryInitializationAotContribution {

        /**
         * Native 支撑配置。
         */
        private final YunyuNativeSupportSettings settings;

        /**
         * 当前类加载器。
         */
        private final ClassLoader classLoader;

        /**
         * 使用配置与类加载器创建贡献对象。
         *
         * @param settings Native 支撑配置
         * @param classLoader 当前类加载器
         */
        private YunyuNativeBeanFactoryInitializationAotContribution(
                YunyuNativeSupportSettings settings,
                ClassLoader classLoader) {
            this.settings = settings;
            this.classLoader = classLoader;
        }

        /**
         * 将 RuntimeHints 应用到当前生成上下文。
         *
         * @param generationContext AOT 生成上下文
         * @param beanFactoryInitializationCode BeanFactory 初始化代码对象
         */
        @Override
        public void applyTo(GenerationContext generationContext, BeanFactoryInitializationCode beanFactoryInitializationCode) {
            YunyuNativeRuntimeHintsRegistrar registrar = new YunyuNativeRuntimeHintsRegistrar();
            ClassLoader effectiveClassLoader = this.classLoader != null
                    ? this.classLoader
                    : Thread.currentThread().getContextClassLoader();
            registrar.registerHints(generationContext.getRuntimeHints(), effectiveClassLoader, settings);
        }
    }
}
