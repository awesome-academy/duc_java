package com.tripgoapi.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tripGoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TripGo API")
                        .description("REST API đặt tour du lịch của TripGo — kiến trúc Hexagonal (Ports and Adapters).")
                        .version("v1")
                        .contact(new Contact().name("TripGo Team")));
    }
}
