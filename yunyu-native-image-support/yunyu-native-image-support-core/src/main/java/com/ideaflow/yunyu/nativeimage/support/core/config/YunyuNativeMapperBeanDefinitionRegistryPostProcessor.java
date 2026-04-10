package com.ideaflow.yunyu.nativeimage.support.core.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Yunyu Native Mapper BeanDefinition 注册后处理器类。
 * 作用：在容器启动早期把标准 `MapperFactoryBean` 定义改写成 Native 友好的 Mapper 工厂定义，避免 Native 运行时把 `Class<?>` 构造参数误判为自动注入依赖。
 */
public class YunyuNativeMapperBeanDefinitionRegistryPostProcessor
        implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

    /**
     * 在 BeanDefinition 注册阶段改写现有 Mapper 定义。
     *
     * @param registry BeanDefinition 注册表
     * @throws BeansException 处理异常
     */
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        List<String> beanNames = new ArrayList<>();
        for (String beanName : registry.getBeanDefinitionNames()) {
            beanNames.add(beanName);
        }
        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (!isStandardMapperFactoryBean(beanDefinition)) {
                continue;
            }
            Class<?> mapperInterface = resolveMapperInterface(beanDefinition);
            if (mapperInterface == null) {
                continue;
            }
            registry.removeBeanDefinition(beanName);
            registry.registerBeanDefinition(beanName, createNativeMapperBeanDefinition(mapperInterface));
        }
    }

    /**
     * BeanFactory 后处理阶段当前无需额外逻辑。
     *
     * @param beanFactory 可配置 BeanFactory
     * @throws BeansException 处理异常
     */
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    }

    /**
     * 返回最高优先级。
     *
     * @return 顺序值
     */
    @Override
    public int getOrder() {
        return PriorityOrdered.HIGHEST_PRECEDENCE;
    }

    /**
     * 判断当前 BeanDefinition 是否是标准 MyBatis MapperFactoryBean 定义。
     *
     * @param beanDefinition BeanDefinition
     * @return 是否为标准 MapperFactoryBean
     */
    private boolean isStandardMapperFactoryBean(BeanDefinition beanDefinition) {
        if (!(beanDefinition instanceof AbstractBeanDefinition abstractBeanDefinition)) {
            return false;
        }
        Class<?> beanClass = null;
        try {
            beanClass = abstractBeanDefinition.getBeanClass();
        } catch (IllegalStateException ignored) {
        }
        if (beanClass != null) {
            return "org.mybatis.spring.mapper.MapperFactoryBean".equals(beanClass.getName());
        }
        return "org.mybatis.spring.mapper.MapperFactoryBean".equals(abstractBeanDefinition.getBeanClassName());
    }

    /**
     * 从 BeanDefinition 中解析 Mapper 接口类型。
     *
     * @param beanDefinition BeanDefinition
     * @return Mapper 接口类型，无法解析时返回 {@code null}
     */
    private Class<?> resolveMapperInterface(BeanDefinition beanDefinition) {
        Object propertyValue = beanDefinition.getPropertyValues().get("mapperInterface");
        Class<?> mapperInterface = resolveMapperInterfaceValue(propertyValue);
        if (mapperInterface != null) {
            return mapperInterface;
        }
        ConstructorArgumentValues constructorArgumentValues = beanDefinition.getConstructorArgumentValues();
        for (ConstructorArgumentValues.ValueHolder valueHolder : constructorArgumentValues.getGenericArgumentValues()) {
            mapperInterface = resolveMapperInterfaceValue(valueHolder.getValue());
            if (mapperInterface != null) {
                return mapperInterface;
            }
        }
        return null;
    }

    /**
     * 解析单个 Mapper 接口配置值。
     *
     * @param value 配置值
     * @return Mapper 接口类型，无法解析时返回 {@code null}
     */
    private Class<?> resolveMapperInterfaceValue(Object value) {
        if (value instanceof Class<?> mapperInterface) {
            return mapperInterface;
        }
        if (value instanceof TypedStringValue typedStringValue) {
            return resolveMapperInterfaceValue(typedStringValue.getValue());
        }
        if (value instanceof String className && !className.isBlank()) {
            return ClassUtils.resolveClassName(className, ClassUtils.getDefaultClassLoader());
        }
        return null;
    }

    /**
     * 创建 Native 友好的 Mapper BeanDefinition。
     *
     * @param mapperInterface Mapper 接口类型
     * @return Native 友好的 Mapper BeanDefinition
     */
    private RootBeanDefinition createNativeMapperBeanDefinition(Class<?> mapperInterface) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(YunyuNativeMapperFactoryBean.class);
        beanDefinition.setTargetType(ResolvableType.forClass(mapperInterface));
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        beanDefinition.setAttribute(FactoryBean.OBJECT_TYPE_ATTRIBUTE, mapperInterface);
        beanDefinition.setLazyInit(true);
        beanDefinition.setInstanceSupplier(() -> createNativeMapperFactoryBean(mapperInterface));
        return beanDefinition;
    }

    /**
     * 创建 Native 友好的 MapperFactoryBean 实例。
     *
     * @param mapperInterface Mapper 接口类型
     * @return Native 友好的 MapperFactoryBean
     */
    private YunyuNativeMapperFactoryBean<?> createNativeMapperFactoryBean(Class<?> mapperInterface) {
        YunyuNativeMapperFactoryBean<?> factoryBean = new YunyuNativeMapperFactoryBean<>();
        factoryBean.setMapperInterface(mapperInterface);
        return factoryBean;
    }
}
