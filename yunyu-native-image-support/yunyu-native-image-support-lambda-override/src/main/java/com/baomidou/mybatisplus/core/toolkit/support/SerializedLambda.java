package com.baomidou.mybatisplus.core.toolkit.support;

import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;
import com.baomidou.mybatisplus.core.toolkit.ClassUtils;

import java.io.Serializable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

/**
 * MyBatis-Plus SerializedLambda 覆盖类。
 * 作用：在 Native Image 环境下避免通过 Java 序列化反推 Lambda 元信息，改为直接调用 `writeReplace` 提取，
 * 从而绕开隐藏 Lambda 类在反序列化阶段的 `ClassNotFoundException` 问题。
 */
public class SerializedLambda implements Serializable {

    /**
     * 序列化版本号。
     */
    private static final long serialVersionUID = 8025925345765570181L;

    /**
     * 捕获当前 Lambda 的宿主类。
     */
    private Class<?> capturingClass;

    /**
     * 函数式接口内部名。
     */
    private String functionalInterfaceClass;

    /**
     * 函数式接口方法名。
     */
    private String functionalInterfaceMethodName;

    /**
     * 函数式接口方法签名。
     */
    private String functionalInterfaceMethodSignature;

    /**
     * 实现类内部名。
     */
    private String implClass;

    /**
     * 实现方法名。
     */
    private String implMethodName;

    /**
     * 实现方法签名。
     */
    private String implMethodSignature;

    /**
     * 实现方法种类。
     */
    private int implMethodKind;

    /**
     * 实例化方法签名。
     */
    private String instantiatedMethodType;

    /**
     * 捕获参数数组。
     */
    private Object[] capturedArgs;

    /**
     * 创建 Lambda 元信息对象。
     * 作用：保留无参构造器，兼容框架内部的反射和序列化约定。
     */
    public SerializedLambda() {
    }

    /**
     * 从可序列化 Lambda 中提取 MyBatis-Plus 所需的元信息。
     *
     * @param serializable Lambda 对象
     * @return MyBatis-Plus SerializedLambda
     */
    public static SerializedLambda extract(Serializable serializable) {
        try {
            java.lang.invoke.SerializedLambda jdkSerializedLambda = extractJdkSerializedLambda(serializable);
            SerializedLambda serializedLambda = new SerializedLambda();
            serializedLambda.capturingClass = resolveCapturingClass(jdkSerializedLambda, serializable);
            serializedLambda.functionalInterfaceClass = jdkSerializedLambda.getFunctionalInterfaceClass();
            serializedLambda.functionalInterfaceMethodName = jdkSerializedLambda.getFunctionalInterfaceMethodName();
            serializedLambda.functionalInterfaceMethodSignature = jdkSerializedLambda.getFunctionalInterfaceMethodSignature();
            serializedLambda.implClass = jdkSerializedLambda.getImplClass();
            serializedLambda.implMethodName = jdkSerializedLambda.getImplMethodName();
            serializedLambda.implMethodSignature = jdkSerializedLambda.getImplMethodSignature();
            serializedLambda.implMethodKind = jdkSerializedLambda.getImplMethodKind();
            serializedLambda.instantiatedMethodType = jdkSerializedLambda.getInstantiatedMethodType();
            serializedLambda.capturedArgs = resolveCapturedArgs(jdkSerializedLambda);
            return serializedLambda;
        } catch (Throwable exception) {
            throw new MybatisPlusException(exception);
        }
    }

    /**
     * 返回实例化方法签名。
     *
     * @return 实例化方法签名
     */
    public String getInstantiatedMethodType() {
        return instantiatedMethodType;
    }

    /**
     * 返回 Lambda 捕获宿主类。
     *
     * @return 捕获宿主类
     */
    public Class<?> getCapturingClass() {
        return capturingClass;
    }

    /**
     * 返回实现方法名。
     *
     * @return 实现方法名
     */
    public String getImplMethodName() {
        return implMethodName;
    }

    /**
     * 直接调用 Lambda 对象的 `writeReplace` 提取 JDK SerializedLambda。
     *
     * @param serializable Lambda 对象
     * @return JDK SerializedLambda
     * @throws Throwable 提取失败时抛出异常
     */
    private static java.lang.invoke.SerializedLambda extractJdkSerializedLambda(Serializable serializable) throws Throwable {
        try {
            Method method = serializable.getClass().getDeclaredMethod("writeReplace");
            method.setAccessible(true);
            return (java.lang.invoke.SerializedLambda) method.invoke(serializable);
        } catch (Throwable reflectionException) {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(serializable.getClass(), MethodHandles.lookup());
            MethodHandle writeReplaceHandle = lookup.findVirtual(
                    serializable.getClass(),
                    "writeReplace",
                    MethodType.methodType(Object.class)
            );
            return (java.lang.invoke.SerializedLambda) writeReplaceHandle.invoke(serializable);
        }
    }

    /**
     * 解析 Lambda 捕获宿主类。
     *
     * @param serializedLambda JDK SerializedLambda
     * @param serializable 原始 Lambda 对象
     * @return 捕获宿主类
     */
    private static Class<?> resolveCapturingClass(java.lang.invoke.SerializedLambda serializedLambda, Serializable serializable) {
        String className = serializedLambda.getCapturingClass().replace('/', '.');
        return ClassUtils.toClassConfident(className, serializable.getClass().getClassLoader());
    }

    /**
     * 读取 Lambda 捕获参数数组。
     *
     * @param serializedLambda JDK SerializedLambda
     * @return 捕获参数数组
     */
    private static Object[] resolveCapturedArgs(java.lang.invoke.SerializedLambda serializedLambda) {
        int capturedArgCount = serializedLambda.getCapturedArgCount();
        Object[] args = new Object[capturedArgCount];
        for (int index = 0; index < capturedArgCount; index++) {
            args[index] = serializedLambda.getCapturedArg(index);
        }
        return args;
    }
}
