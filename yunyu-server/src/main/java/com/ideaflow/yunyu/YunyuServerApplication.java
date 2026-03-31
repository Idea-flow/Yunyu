package com.ideaflow.yunyu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Yunyu 后端应用启动类。
 * 作用：作为 Spring Boot 应用入口，负责启动自动配置、组件扫描和整个后端服务。
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class YunyuServerApplication {

	/**
	 * 启动 Yunyu 后端应用。
	 *
	 * @param args 启动参数
	 */
	public static void main(String[] args) {
		SpringApplication.run(YunyuServerApplication.class, args);
	}

}
