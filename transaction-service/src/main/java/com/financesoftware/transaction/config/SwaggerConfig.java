package com.financesoftware.transaction.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .servers(List.of(new Server().url("/")))
                                .info(new Info()
                                                .title("Transaction Service API")
                                                .description("REST API for managing transactions in the finance system")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("financesoftware Development Team")
                                                                .email("dev@financesoftware.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0")));
        }
}
