package com.dinoventures.wallet.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class SecretsManagerConfig {

    private static final Logger log = LoggerFactory.getLogger(SecretsManagerConfig.class);

    @Value("${spring.datasource.url:not-set}")
    private String datasourceUrl;

    @PostConstruct
    public void logSecretsStatus() {
        if ("not-set".equals(datasourceUrl) || "placeholder".equals(datasourceUrl)) {
            log.warn("DataSource URL not resolved — AWS Secrets Manager may not be configured correctly");
        } else {
            String host = datasourceUrl.replaceAll("jdbc:postgresql://([^:/]+).*", "$1");
            log.info("Database credentials loaded from AWS Secrets Manager (host: {})", host);
        }
    }
}
