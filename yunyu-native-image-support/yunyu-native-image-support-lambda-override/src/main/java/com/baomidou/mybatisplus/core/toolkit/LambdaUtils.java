package com.baomidou.mybatisplus.core.toolkit;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.support.ColumnCache;
import com.baomidou.mybatisplus.core.toolkit.support.IdeaProxyLambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.LambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.ReflectLambdaMeta;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda;
import com.baomidou.mybatisplus.core.toolkit.support.ShadowLambdaMeta;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis-Plus Lambda 工具覆盖类。
 * 作用：为 Native Image 环境下的 Lambda 字段解析提供更稳定的实现，降低默认反序列化路径的不确定性。
 */
public final class LambdaUtils {

    /**
     * 实体字段与列信息缓存。
     */
    private static final Map<String, Map<String, ColumnCache>> COLUMN_CACHE_MAP = new ConcurrentHashMap<>();

    /**
     * 私有构造方法。
     * 作用：禁止工具类被实例化。
     */
    private LambdaUtils() {
    }

    /**
     * 提取 Lambda 元信息。
     *
     * @param func 字段方法引用
     * @param <T> 实体类型
     * @return Lambda 元信息
     */
    public static <T> LambdaMeta extract(SFunction<T, ?> func) {
        if (func instanceof Proxy proxy) {
            return new IdeaProxyLambdaMeta(proxy);
        }
        try {
            return new ReflectLambdaMeta(extractSerializedLambda(func), func.getClass().getClassLoader());
        } catch (Throwable exception) {
            return new ShadowLambdaMeta(SerializedLambda.extract(func));
        }
    }

    /**
     * 格式化字段缓存键。
     *
     * @param key 原始字段名
     * @return 标准化后的缓存键
     */
    public static String formatKey(String key) {
        return key.toUpperCase(Locale.ENGLISH);
    }

    /**
     * 安装实体字段缓存。
     *
     * @param tableInfo 表信息
     */
    public static void installCache(TableInfo tableInfo) {
        COLUMN_CACHE_MAP.put(tableInfo.getEntityType().getName(), createColumnCacheMap(tableInfo));
    }

    /**
     * 获取实体字段缓存映射。
     *
     * @param clazz 实体类型
     * @return 字段缓存映射
     */
    public static Map<String, ColumnCache> getColumnMap(Class<?> clazz) {
        return CollectionUtils.computeIfAbsent(COLUMN_CACHE_MAP, clazz.getName(), key -> {
            TableInfo tableInfo = TableInfoHelper.getTableInfo(clazz);
            return tableInfo == null ? null : createColumnCacheMap(tableInfo);
        });
    }

    /**
     * 创建字段缓存映射。
     *
     * @param tableInfo 表信息
     * @return 字段缓存映射
     */
    private static Map<String, ColumnCache> createColumnCacheMap(TableInfo tableInfo) {
        Map<String, ColumnCache> columnCacheMap;
        if (tableInfo.havePK()) {
            columnCacheMap = CollectionUtils.newHashMapWithExpectedSize(tableInfo.getFieldList().size() + 1);
            columnCacheMap.put(
                    formatKey(tableInfo.getKeyProperty()),
                    new ColumnCache(tableInfo.getKeyColumn(), tableInfo.getKeySqlSelect())
            );
        } else {
            columnCacheMap = CollectionUtils.newHashMapWithExpectedSize(tableInfo.getFieldList().size());
        }
        tableInfo.getFieldList().forEach(fieldInfo -> putColumnCache(columnCacheMap, fieldInfo));
        return columnCacheMap;
    }

    /**
     * 写入单个字段缓存。
     *
     * @param columnCacheMap 字段缓存容器
     * @param fieldInfo 字段元信息
     */
    private static void putColumnCache(Map<String, ColumnCache> columnCacheMap, TableFieldInfo fieldInfo) {
        columnCacheMap.put(
                formatKey(fieldInfo.getProperty()),
                new ColumnCache(fieldInfo.getColumn(), fieldInfo.getSqlSelect(), fieldInfo.getMapping())
        );
    }

    /**
     * 提取 JDK `SerializedLambda`。
     *
     * @param func 字段方法引用
     * @param <T> 实体类型
     * @return JDK SerializedLambda
     * @throws Throwable 提取失败时抛出异常
     */
    private static <T> java.lang.invoke.SerializedLambda extractSerializedLambda(SFunction<T, ?> func) throws Throwable {
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            return (java.lang.invoke.SerializedLambda) method.invoke(func);
        } catch (Throwable reflectionException) {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(func.getClass(), MethodHandles.lookup());
            MethodHandle writeReplaceHandle = lookup.findVirtual(
                    func.getClass(),
                    "writeReplace",
                    MethodType.methodType(Object.class)
            );
            return (java.lang.invoke.SerializedLambda) writeReplaceHandle.invoke(func);
        }
    }
}
