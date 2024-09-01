package com.example.board.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // http://localhost:8080/swagger-ui/index.html
        return new OpenAPI()
                .info(new Info()
                        .title("sample-board")
                        .version("1.0")
                        .description("This is a sample API using Spring Boot and OpenAPI"))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local server")
                ));
    }
}