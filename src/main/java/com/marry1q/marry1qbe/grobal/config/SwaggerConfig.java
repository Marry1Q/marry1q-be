package com.marry1q.marry1qbe.grobal.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .title("Marry1Q BackEnd Server")
                .description("Marry1Q 백엔드 API 문서")
                .version("v1.0.0")
                .license(new License().name("Apache 2.0").url("http://springdoc.org"));

        // JWT Bearer 토큰 인증 스키마 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization")
                .description("JWT Bearer 토큰을 입력하세요. 예: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");

        return new OpenAPI()
                .info(info)
                .servers(List.of(
                    new Server().url("https://api.marry1q.com").description("Production Server"),
                    new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", securityScheme));
    }
}
