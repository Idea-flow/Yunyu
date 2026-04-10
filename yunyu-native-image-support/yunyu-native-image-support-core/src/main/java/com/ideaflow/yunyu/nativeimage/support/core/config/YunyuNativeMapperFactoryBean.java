package com.ideaflow.yunyu.nativeimage.support.core.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartFactoryBean;

/**
 * Yunyu Native Mapper 工厂 Bean 类。
 * 作用：在 Spring AOT 与 Native 运行时按统一方式创建 Mapper 代理对象，避免标准 MapperFactoryBean 的构造参数在 Native 下被误判为自动注入依赖。
 *
 * @param <T> Mapper 接口类型
 */
public class YunyuNativeMapperFactoryBean<T> implements SmartFactoryBean<T>, BeanFactoryAware {

    /**
     * 当前工厂负责创建的 Mapper 接口类型。
     */
    private Class<?> mapperInterface;

    /**
     * MyBatis 的 SqlSessionTemplate。
     */
    private SqlSessionTemplate sqlSessionTemplate;

    /**
     * MyBatis 的 SqlSessionFactory。
     */
    private SqlSessionFactory sqlSessionFactory;

    /**
     * Spring BeanFactory。
     */
    private BeanFactory beanFactory;

    /**
     * 创建 Native 友好的 Mapper 工厂 Bean。
     * 作用：显式保留无参构造器，兼容 Spring AOT 在 Native 模式下的实例化路径。
     */
    public YunyuNativeMapperFactoryBean() {
    }

    /**
     * 设置当前工厂负责创建的 Mapper 接口类型。
     *
     * @param mapperInterface Mapper 接口类型
     */
    public void setMapperInterface(Class<?> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * 返回当前工厂负责创建的 Mapper 接口类型。
     *
     * @return Mapper 接口类型
     */
    public Class<?> getMapperInterface() {
        return mapperInterface;
    }

    /**
     * 设置 `SqlSessionTemplate`。
     *
     * @param sqlSessionTemplate MyBatis 的 SqlSessionTemplate
     */
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
        this.sqlSessionTemplate = sqlSessionTemplate;
    }

    /**
     * 设置 `SqlSessionFactory`。
     *
     * @param sqlSessionFactory MyBatis 的 SqlSessionFactory
     */
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 注入 Spring BeanFactory。
     *
     * @param beanFactory Spring BeanFactory
     * @throws BeansException 注入异常
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * 创建 Mapper 代理对象。
     *
     * @return Mapper 代理实例
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        if (mapperInterface == null) {
            throw new IllegalStateException("YunyuNativeMapperFactoryBean 未获取到 mapperInterface，无法创建 Mapper 代理");
        }
        Class<T> currentMapperInterface = (Class<T>) mapperInterface;
        SqlSessionFactory currentSqlSessionFactory = resolveSqlSessionFactory();
        SqlSessionTemplate currentSqlSessionTemplate = resolveSqlSessionTemplate();
        if (!currentSqlSessionFactory.getConfiguration().hasMapper(currentMapperInterface)) {
            currentSqlSessionFactory.getConfiguration().addMapper(currentMapperInterface);
        }
        return currentSqlSessionTemplate.getMapper(currentMapperInterface);
    }

    /**
     * 返回当前 FactoryBean 生产的对象类型。
     *
     * @return Mapper 接口类型
     */
    @Override
    public Class<?> getObjectType() {
        return mapperInterface;
    }

    /**
     * 声明当前工厂创建的是单例 Mapper 代理。
     *
     * @return 始终返回 true
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * 声明当前 FactoryBean 不需要在容器启动早期急切初始化。
     *
     * @return 始终返回 false
     */
    @Override
    public boolean isEagerInit() {
        return false;
    }

    /**
     * 解析 `SqlSessionTemplate`。
     *
     * @return SqlSessionTemplate 实例
     */
    private SqlSessionTemplate resolveSqlSessionTemplate() {
        if (sqlSessionTemplate == null) {
            if (beanFactory == null) {
                throw new IllegalStateException("SqlSessionTemplate 尚未初始化，且 BeanFactory 不可用");
            }
            sqlSessionTemplate = beanFactory.getBean(SqlSessionTemplate.class);
        }
        return sqlSessionTemplate;
    }

    /**
     * 解析 `SqlSessionFactory`。
     *
     * @return SqlSessionFactory 实例
     */
    private SqlSessionFactory resolveSqlSessionFactory() {
        if (sqlSessionFactory == null) {
            if (beanFactory == null) {
                throw new IllegalStateException("SqlSessionFactory 尚未初始化，且 BeanFactory 不可用");
            }
            sqlSessionFactory = beanFactory.getBean(SqlSessionFactory.class);
        }
        return sqlSessionFactory;
    }
}
