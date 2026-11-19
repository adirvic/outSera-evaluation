package com.outsera.razzies.shared.adapter.in.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI razziesOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Golden Raspberry Awards API")
                .description("Nominees and winners of the Worst Picture category")
                .version("v1"));
    }
}
