package com.dinoventures.wallet.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI walletServiceOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Wallet Service API")
                                                .description("Internal Wallet Service — Closed-loop virtual currency management "
                                                                +
                                                                "for gaming/loyalty platforms. Manages asset types, user wallets, "
                                                                +
                                                                "and an immutable transaction ledger with concurrency safety and idempotency.")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Dino Ventures Engineering")
                                                                .email("engineering@dinoventures.com")))
                                .servers(List.of(
                                                new Server().url("https://dinoventures.duckdns.org")
                                                                .description("Production Server"),
                                                new Server().url("http://localhost:8080")
                                                                .description("Local Development")));
        }
}
