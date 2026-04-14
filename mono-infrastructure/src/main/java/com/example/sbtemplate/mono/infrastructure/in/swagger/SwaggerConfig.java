package com.example.sbtemplate.mono.infrastructure.in.swagger;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI usersOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Users Service API")
                        .description("CRUD endpoints for managing users.")
                        .version("1.0.0")
                        .contact(new Contact().name("Users Team").email("support@example.com"))
                        .license(new License().name("Apache 2.0")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project repository")
                        .url("https://example.com/repo"));
    }
}
