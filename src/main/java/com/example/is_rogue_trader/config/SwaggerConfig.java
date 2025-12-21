package com.example.is_rogue_trader.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Rogue Trader API")
                        .version("1.0.0")
                        .description("REST API для информационной системы управления вольными торговцами. " +
                                "Система позволяет управлять планетами, событиями, проектами и ресурсами империи. " +
                                "Использует PL/pgSQL функции для критически важных операций: " +
                                "send_message(), resolve_crisis(), get_empire_resources().\n\n" +
                                "**Авторизация:** Используйте эндпоинты /api/auth/register или /api/auth/login для получения JWT токена. " +
                                "Затем добавьте токен в заголовок Authorization: Bearer <token> для доступа к защищенным эндпоинтам.")
                        .contact(new Contact()
                                .name("Rogue Trader System")
                                .email("support@rogue-trader.example.com")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Введите JWT токен, полученный при авторизации")));
    }
}

