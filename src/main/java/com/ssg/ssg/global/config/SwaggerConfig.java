package com.ssg.ssg.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.Getter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String BASE_PACKAGE = "com.ssg.ssg.domain";

    @Getter
    public enum ApiUrl {
        ORDER("order", "/orders");

        private final String group;
        private final String urlPrefix;

        ApiUrl(String group, String urlPrefix) {
            this.group = group;
            this.urlPrefix = urlPrefix;
        }
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("주문 서비스 API")
                        .description("주문 서비스 API 문서"));
    }

    @Bean
    public GroupedOpenApi orderApi() {
        final String name = ApiUrl.ORDER.getGroup();
        return GroupedOpenApi.builder()
                .group(name)
                .pathsToMatch(ApiUrl.ORDER.getUrlPrefix() + "/**")
                .packagesToScan(BASE_PACKAGE + "." + ApiUrl.ORDER.getGroup())
                .build();
    }

}
