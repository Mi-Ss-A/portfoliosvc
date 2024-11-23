package com.wibeechat.missa.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@OpenAPIDefinition(info = @Info(title = "사용자 API 명세서", description = "사용자 서비스 API 문서", version = "v1.0"))
@Configuration
public class SwaggerConfig {

        @SuppressWarnings("unused")
        @Bean
        public OpenAPI openAPI() {
                SecurityScheme cookieAuth = new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID");

                return new OpenAPI()
                                .components(new Components()
                                                .addSecuritySchemes("Session",
                                                                new SecurityScheme()
                                                                                .type(SecurityScheme.Type.APIKEY)
                                                                                .in(SecurityScheme.In.COOKIE)
                                                                                .name("JSESSIONID")))
                                .addSecurityItem(new SecurityRequirement().addList("Session"));
        }

        // Swagger UI가 세션 쿠키를 유지하도록 설정
        @Bean
        public GroupedOpenApi publicApi() {
                return GroupedOpenApi.builder()
                                .group("public")
                                .pathsToMatch("/api/**")
                                .build();
        }
}