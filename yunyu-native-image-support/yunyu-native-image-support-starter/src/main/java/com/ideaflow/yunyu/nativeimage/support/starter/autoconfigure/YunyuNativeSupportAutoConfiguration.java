package com.ideaflow.yunyu.nativeimage.support.starter.autoconfigure;

import com.ideaflow.yunyu.nativeimage.support.core.aot.YunyuNativeBeanFactoryInitializationAotProcessor;
import com.ideaflow.yunyu.nativeimage.support.core.config.YunyuNativeMapperBeanDefinitionRegistryPostProcessor;
import com.ideaflow.yunyu.nativeimage.support.core.config.YunyuNativeSupportConfiguration;
import com.ideaflow.yunyu.nativeimage.support.starter.properties.YunyuNativeProperties;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.aot.BeanFactoryInitializationAotProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Yunyu Native Starter 自动配置类。
 * 作用：在业务工程只引入 starter 依赖时，自动接通基础 RuntimeHints 与业务包扫描型 AOT 增强能力。
 */
@AutoConfiguration
@ConditionalOnClass(SqlSessionFactory.class)
@ConditionalOnProperty(prefix = "yunyu.native", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(YunyuNativeProperties.class)
@Import(YunyuNativeSupportConfiguration.class)
public class YunyuNativeSupportAutoConfiguration {

    /**
     * 注册 BeanFactory AOT 处理器。
     * 作用：在 Spring AOT 阶段按主包与扩展包配置补充业务相关 RuntimeHints。
     *
     * @return BeanFactory AOT 处理器
     */
    @Bean
    public static BeanFactoryInitializationAotProcessor yunyuNativeBeanFactoryInitializationAotProcessor() {
        return new YunyuNativeBeanFactoryInitializationAotProcessor();
    }

    /**
     * 注册 Mapper BeanDefinition 改写后处理器。
     * 作用：把标准 MyBatis MapperFactoryBean 定义改写成更适合 Native 运行时的工厂定义。
     *
     * @return Mapper BeanDefinition 注册后处理器
     */
    @Bean
    public static BeanDefinitionRegistryPostProcessor yunyuNativeMapperBeanDefinitionRegistryPostProcessor() {
        return new YunyuNativeMapperBeanDefinitionRegistryPostProcessor();
    }
}
