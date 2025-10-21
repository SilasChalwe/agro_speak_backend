package com.nextinnomind.agro_speak_backend.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agroSpeakOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Agro Speak Backend API")
                        .description("API documentation for Agro Speak project.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("CovianHive Team")
                                .email("support@covianhive.com")
                                .url("https://covianhive.nextinnomind.me"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Project Documentation")
                        .url("https://github.com/SilasChalwe/agro_speak_backend"));
    }
}
