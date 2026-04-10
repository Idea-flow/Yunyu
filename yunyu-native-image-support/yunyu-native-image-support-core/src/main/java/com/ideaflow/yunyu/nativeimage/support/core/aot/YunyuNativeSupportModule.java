package com.ideaflow.yunyu.nativeimage.support.core.aot;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisXMLLanguageDriver;
import com.baomidou.mybatisplus.core.conditions.AbstractLambdaWrapper;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.SharedString;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.AbstractISegmentList;
import com.baomidou.mybatisplus.core.conditions.segments.GroupBySegmentList;
import com.baomidou.mybatisplus.core.conditions.segments.HavingSegmentList;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.core.conditions.segments.NormalSegmentList;
import com.baomidou.mybatisplus.core.conditions.segments.OrderBySegmentList;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.MapperProxyMetadata;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import com.baomidou.mybatisplus.core.override.MybatisMapperProxyFactory;
import com.baomidou.mybatisplus.core.toolkit.MybatisUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.ideaflow.yunyu.nativeimage.support.core.config.YunyuNativeSupportSettings;
import org.apache.ibatis.cache.decorators.FifoCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.SoftCache;
import org.apache.ibatis.cache.decorators.WeakCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.javassist.util.proxy.ProxyFactory;
import org.apache.ibatis.javassist.util.proxy.RuntimeSupport;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl;
import org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl;
import org.apache.ibatis.logging.log4j2.Log4j2Impl;
import org.apache.ibatis.logging.nologging.NoLoggingImpl;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.defaults.RawLanguageDriver;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.graalvm.nativeimage.hosted.RuntimeProxyCreation;
import org.graalvm.nativeimage.hosted.RuntimeReflection;
import org.graalvm.nativeimage.hosted.RuntimeResourceAccess;
import org.graalvm.nativeimage.hosted.RuntimeSerialization;

import java.lang.invoke.SerializedLambda;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Yunyu Native 支撑基础模块类。
 * 作用：在 GraalVM Feature 阶段集中补齐 MyBatis-Plus Lambda、Wrapper、分页和基础设施所需的运行期元数据。
 */
public class YunyuNativeSupportModule {

    /**
     * Native 构建期类扫描器。
     */
    private final NativeImageClassScanner scanner;

    /**
     * Native 支撑配置。
     */
    private final YunyuNativeSupportSettings settings;

    /**
     * 使用扫描器和配置创建模块实例。
     *
     * @param scanner Native 构建期类扫描器
     * @param settings Native 支撑配置
     */
    public YunyuNativeSupportModule(NativeImageClassScanner scanner, YunyuNativeSupportSettings settings) {
        this.scanner = scanner;
        this.settings = settings;
    }

    /**
     * 执行全部 Native 元数据注册。
     * 作用：统一收口 Lambda、Mapper、Wrapper 和 MyBatis 基础设施的注册流程。
     */
    public void registerAll() {
        registerLambdaSupport();
        registerMapperSupport();
        registerWrapperSupport();
        registerMyBatisInfrastructure();
    }

    /**
     * 注册 Lambda 相关序列化与捕获类支持。
     * 作用：确保业务包中的 `$deserializeLambda$` 宿主类在 Native 运行期可参与 Lambda 反序列化解析。
     */
    public void registerLambdaSupport() {
        RuntimeSerialization.register(SerializedLambda.class);
        RuntimeSerialization.register(SFunction.class);

        for (Class<?> applicationClass : scanner.findProjectClasses(settings.getScanPackages())) {
            registerReflection(applicationClass);
            if (scanner.isLambdaCapturingClass(applicationClass)) {
                RuntimeSerialization.registerLambdaCapturingClass(applicationClass);
            }
        }
    }

    /**
     * 注册 Mapper 接口反射、代理和 XML 资源支持。
     * 作用：确保业务 Mapper 在 Native 运行期可正常创建代理并解析 XML 映射。
     */
    public void registerMapperSupport() {
        for (Class<?> mapperInterface : settings.resolveMapperInterfaces(scanner)) {
            registerReflection(mapperInterface);
            registerProxy(mapperInterface);
            registerMapperXmlResource(mapperInterface);
        }
    }

    /**
     * 注册 Wrapper 与分页体系所需的反射元数据。
     * 作用：确保 `LambdaQueryWrapper` 等常用条件构造器在 Native 运行期可正常工作。
     */
    public void registerWrapperSupport() {
        Stream.of(
                Wrapper.class,
                AbstractWrapper.class,
                AbstractLambdaWrapper.class,
                QueryWrapper.class,
                LambdaQueryWrapper.class,
                UpdateWrapper.class,
                LambdaUpdateWrapper.class,
                Wrappers.class,
                SharedString.class,
                MergeSegments.class,
                AbstractISegmentList.class,
                NormalSegmentList.class,
                GroupBySegmentList.class,
                HavingSegmentList.class,
                OrderBySegmentList.class,
                IPage.class,
                Page.class,
                PageDTO.class,
                OrderItem.class,
                MybatisPlusInterceptor.class,
                PaginationInnerInterceptor.class
        ).forEach(this::registerReflection);

        registerReflection(scanner.loadClass("com.baomidou.mybatisplus.core.toolkit.LambdaUtils"));
        registerReflection(scanner.loadClass("com.baomidou.mybatisplus.core.toolkit.Wrappers$EmptyWrapper"));

        Set<Class<?>> wrapperTypes = scanner.collectClasses(
                "com.baomidou.mybatisplus",
                wrapperType -> Wrapper.class.isAssignableFrom(wrapperType)
        );
        for (Class<?> wrapperType : wrapperTypes) {
            registerReflection(wrapperType);
        }
    }

    /**
     * 注册 MyBatis 基础设施所需的反射和动态代理支持。
     * 作用：确保分页插件、Mapper 默认方法、缓存装饰器等基础设施在 Native 运行期可正常工作。
     */
    public void registerMyBatisInfrastructure() {
        Stream.of(
                RawLanguageDriver.class,
                XMLLanguageDriver.class,
                MybatisXMLLanguageDriver.class,
                RuntimeSupport.class,
                ProxyFactory.class,
                Slf4jImpl.class,
                Log.class,
                JakartaCommonsLoggingImpl.class,
                Log4j2Impl.class,
                Jdk14LoggingImpl.class,
                StdOutImpl.class,
                NoLoggingImpl.class,
                MybatisConfiguration.class,
                MybatisUtils.class,
                PluginUtils.class,
                PluginUtils.MPBoundSql.class,
                PluginUtils.MPStatementHandler.class,
                MapperProxyMetadata.class,
                MybatisMapperProxy.class,
                MybatisMapperProxyFactory.class,
                BoundSql.class,
                ParameterMapping.class,
                MetaObject.class,
                DefaultReflectorFactory.class,
                PerpetualCache.class,
                FifoCache.class,
                LruCache.class,
                SoftCache.class,
                WeakCache.class
        ).forEach(this::registerReflection);

        Stream.of(
                Executor.class,
                ParameterHandler.class,
                ResultSetHandler.class,
                StatementHandler.class,
                SFunction.class
        ).forEach(this::registerProxy);

        registerResource(YunyuNativeSupportModule.class.getModule(), "org/apache/ibatis/builder/xml/mybatis-3-mapper.dtd");
        registerResource(YunyuNativeSupportModule.class.getModule(), "org/apache/ibatis/builder/xml/mybatis-3-config.dtd");
        registerResource(YunyuNativeSupportModule.class.getModule(), "org/apache/ibatis/builder/xml/mybatis-mapper.xsd");
        registerResource(YunyuNativeSupportModule.class.getModule(), "org/apache/ibatis/builder/xml/mybatis-config.xsd");
    }

    /**
     * 为指定类型注册完整反射访问权限。
     *
     * @param type 待注册类型
     */
    public void registerReflection(Class<?> type) {
        if (type == null) {
            return;
        }
        try {
            RuntimeReflection.register(type);
            RuntimeReflection.register(type.getDeclaredConstructors());
            RuntimeReflection.register(type.getDeclaredMethods());
            RuntimeReflection.register(type.getDeclaredFields());
        } catch (LinkageError ignored) {
        }
    }

    /**
     * 为指定接口注册 JDK 动态代理支持。
     *
     * @param interfaceType 代理接口类型
     */
    public void registerProxy(Class<?> interfaceType) {
        if (interfaceType == null) {
            return;
        }
        try {
            RuntimeProxyCreation.register(interfaceType);
        } catch (LinkageError ignored) {
        }
    }

    /**
     * 为指定 Mapper 注册同包同名 XML 资源。
     *
     * @param mapperInterface Mapper 接口类型
     */
    public void registerMapperXmlResource(Class<?> mapperInterface) {
        if (mapperInterface == null) {
            return;
        }
        registerResource(mapperInterface.getModule(), mapperInterface.getName().replace('.', '/').concat(".xml"));
    }

    /**
     * 注册单个资源路径到 Native Image。
     *
     * @param module 资源所在模块
     * @param resourcePath 资源路径
     */
    public void registerResource(Module module, String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            return;
        }
        try {
            RuntimeResourceAccess.addResource(module, resourcePath);
        } catch (IllegalArgumentException ignored) {
        } catch (LinkageError ignored) {
        }
    }
}
