package com.ideaflow.yunyu.infrastructure.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置类。
 * 作用：统一定义 Yunyu 后端接口文档的基础信息，供 Swagger UI 页面展示。
 */
@Configuration
public class OpenApiConfig {

    /**
     * 构建 OpenAPI 文档基础信息。
     *
     * @return OpenAPI 对象
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Yunyu Server API")
                        .description("云屿后端接口文档")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("IdeaFlow")
                                .email("support@ideaflow.com")));
    }
}
