package com.ideaflow.yunyu;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

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
	public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(YunyuServerApplication.class, args);

        Environment env = application.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        String path = env.getProperty("server.servlet.context-path");
        path = StringUtils.isNotBlank(path) ? path : "";
        System.out.println("\n----------------------------------------------------------\n\t" +
                "blog is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "swagger-ui: \thttp://" + ip + ":" + port + path + "/swagger-ui.html\n\t" +
                "api-docs: \thttp://" + ip + ":" + port + path + "/v3/api-docs\n\t" +
                "----------------------------------------------------------");
	}

}
