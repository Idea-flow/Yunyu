package com.ideaflow.yunyu.nativeimage.support.core.aot;

import com.baomidou.mybatisplus.annotation.DbType;
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
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.MapperProxyMetadata;
import com.baomidou.mybatisplus.core.override.MybatisMapperMethod;
import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import com.baomidou.mybatisplus.core.override.MybatisMapperProxyFactory;
import com.baomidou.mybatisplus.core.toolkit.LambdaUtils;
import com.baomidou.mybatisplus.core.toolkit.MybatisUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.IdeaProxyLambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.ReflectLambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import com.baomidou.mybatisplus.core.toolkit.support.ShadowLambdaMeta;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectModel;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.IDialect;
import com.baomidou.mybatisplus.extension.plugins.pagination.dialects.MySqlDialect;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.ideaflow.yunyu.nativeimage.support.core.config.YunyuNativeSupportSettings;
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
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Yunyu Native RuntimeHints 注册类。
 * 作用：统一注册 MyBatis-Plus、业务实体、Mapper 与资源文件在 Native Image 下所需的运行时提示。
 */
public class YunyuNativeRuntimeHintsRegistrar implements RuntimeHintsRegistrar {

    /**
     * 注册框架基础类型所需的 RuntimeHints。
     *
     * @param hints RuntimeHints 对象
     * @param classLoader 当前类加载器
     */
    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        registerFrameworkHints(hints, classLoader);
    }

    /**
     * 注册业务项目相关的 RuntimeHints。
     *
     * @param hints RuntimeHints 对象
     * @param classLoader 当前类加载器
     * @param settings Native 扫描配置
     */
    public void registerHints(RuntimeHints hints, ClassLoader classLoader, YunyuNativeSupportSettings settings) {
        registerFrameworkHints(hints, classLoader);
        if (settings == null || settings.getScanPackages().isEmpty()) {
            return;
        }
        NativeImageClassScanner scanner = new NativeImageClassScanner(classLoader);
        Set<Class<?>> mapperInterfaces = scanner.findMapperInterfaces(settings.getScanPackages());
        Set<Class<?>> entityClasses = scanner.findEntityClasses(mapperInterfaces);
        Set<Class<?>> lambdaCapturingClasses = scanner.findLambdaCapturingClasses(settings.getScanPackages());
        Set<Class<?>> projectClasses = scanner.findProjectClasses(settings.getScanPackages());

        for (Class<?> mapperInterface : mapperInterfaces) {
            hints.reflection().registerType(mapperInterface, MemberCategory.values());
            hints.proxies().registerJdkProxy(mapperInterface);
        }
        for (Class<?> entityClass : entityClasses) {
            hints.reflection().registerType(entityClass, MemberCategory.values());
        }
        for (Class<?> lambdaCapturingClass : lambdaCapturingClasses) {
            hints.reflection().registerType(lambdaCapturingClass, MemberCategory.values());
        }
        for (Class<?> projectClass : projectClasses) {
            hints.reflection().registerType(projectClass, MemberCategory.values());
        }
    }

    /**
     * 注册框架层基础 RuntimeHints。
     *
     * @param hints RuntimeHints 对象
     * @param classLoader 当前类加载器
     */
    private void registerFrameworkHints(RuntimeHints hints, ClassLoader classLoader) {
        Stream.of(
                RawLanguageDriver.class,
                XMLLanguageDriver.class,
                MybatisXMLLanguageDriver.class,
                RuntimeSupport.class,
                ProxyFactory.class,
                Log.class,
                JakartaCommonsLoggingImpl.class,
                Jdk14LoggingImpl.class,
                Log4j2Impl.class,
                NoLoggingImpl.class,
                Slf4jImpl.class,
                StdOutImpl.class,
                Executor.class,
                ParameterHandler.class,
                ResultSetHandler.class,
                StatementHandler.class,
                SqlSessionFactory.class,
                MybatisConfiguration.class,
                MybatisSqlSessionFactoryBean.class,
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
                TableInfo.class,
                TableFieldInfo.class,
                LambdaUtils.class,
                MybatisUtils.class,
                LambdaMeta.class,
                ReflectLambdaMeta.class,
                ShadowLambdaMeta.class,
                IdeaProxyLambdaMeta.class,
                SerializedLambda.class,
                ColumnCache.class,
                SFunction.class,
                MapperProxyMetadata.class,
                MybatisMapperMethod.class,
                MybatisMapperProxy.class,
                MybatisMapperProxyFactory.class,
                PluginUtils.class,
                PluginUtils.MPBoundSql.class,
                PluginUtils.MPStatementHandler.class,
                IPage.class,
                Page.class,
                PageDTO.class,
                OrderItem.class,
                MybatisPlusInterceptor.class,
                InnerInterceptor.class,
                PaginationInnerInterceptor.class,
                DialectFactory.class,
                DialectModel.class,
                IDialect.class,
                MySqlDialect.class,
                DbType.class,
                BoundSql.class,
                ParameterMapping.class,
                MetaObject.class,
                DefaultReflectorFactory.class,
                ArrayList.class,
                HashMap.class,
                HashSet.class,
                TreeSet.class
        ).forEach(type -> hints.reflection().registerType(type, MemberCategory.values()));

        hints.reflection().registerType(
                TypeReference.of("com.baomidou.mybatisplus.core.toolkit.Wrappers$EmptyWrapper"),
                MemberCategory.values()
        );
        hints.serialization().registerType(java.lang.invoke.SerializedLambda.class);
        hints.serialization().registerType(SerializedLambda.class);
        hints.proxies().registerJdkProxy(Executor.class);
        hints.proxies().registerJdkProxy(ParameterHandler.class);
        hints.proxies().registerJdkProxy(ResultSetHandler.class);
        hints.proxies().registerJdkProxy(StatementHandler.class);
        hints.proxies().registerJdkProxy(SFunction.class);
        hints.resources().registerPattern("application.yml");
        hints.resources().registerPattern("application-*.yml");
        hints.resources().registerPattern("db/init/*.sql");
        hints.resources().registerPattern("META-INF/spring/*");
        hints.resources().registerPatternIfPresent(classLoader, "META-INF/native-image", builder -> {
        });
    }
}
