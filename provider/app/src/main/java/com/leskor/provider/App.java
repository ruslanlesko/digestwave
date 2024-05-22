package com.leskor.provider;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("Digestwave Provider API")
                        .description("API which provides access to the Digestwave backend (news articles) for client application")
                        .version("0.1.0")
                        .contact(new Contact().name("Ruslan Lesko").url("https://leskor.com").email("ruslanlesko@gmail.com"))
                        .license(new License().name("MIT License").url("https://mit-license.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("Digestwave Provider API GitHub Documentation")
                        .url("https://github.com/ruslanlesko/digestwave/tree/main/provider"));
    }
}

