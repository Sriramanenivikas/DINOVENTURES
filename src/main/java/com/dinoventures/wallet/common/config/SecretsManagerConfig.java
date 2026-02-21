package com.dinoventures.wallet.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

/**
 * AWS Secrets Manager Integration — How It Works
 * ================================================
 *
 * This project uses Spring Cloud AWS 3.x to load database credentials
 * from AWS Secrets Manager BEFORE the application context starts.
 *
 * <h3>Mechanism:</h3>
 * 
 * <pre>
 * 1. application.properties has:
 *    spring.config.import=aws-secretsmanager:wallet-service/database
 *
 * 2. Spring Boot sees "aws-secretsmanager:" prefix and uses the
 *    Spring Cloud AWS PropertySource to fetch the secret.
 *
 * 3. The secret JSON keys become Spring properties:
 *    { "url": "jdbc:...", "username": "admin", "password": "..." }
 *    →  url=jdbc:...
 *    →  username=admin
 *    →  password=...
 *
 * 4. application.properties references them via ${url}, ${username}, ${password}:
 *    spring.datasource.url=${url}
 *    spring.datasource.username=${username}
 *    spring.datasource.password=${password}
 *
 * 5. By the time HikariCP initializes, the real DB credentials are already available.
 * </pre>
 *
 * <h3>AWS Credential Resolution (Default Provider Chain):</h3>
 * <ul>
 * <li><b>Local dev:</b> ~/.aws/credentials (run "aws configure") or
 * environment variables AWS_ACCESS_KEY_ID + AWS_SECRET_ACCESS_KEY</li>
 * <li><b>EC2:</b> IAM Instance Role — automatic, zero config</li>
 * <li><b>ECS/Fargate:</b> IAM Task Role — automatic, zero config</li>
 * <li><b>Lambda:</b> IAM Execution Role — automatic, zero config</li>
 * </ul>
 *
 * <h3>The secret in AWS Secrets Manager must have this JSON structure:</h3>
 * 
 * <pre>
 * Secret name: wallet-service/database
 * {
 *   "url": "jdbc:postgresql://your-rds-endpoint:5432/wallet_db",
 *   "username": "wallet_admin",
 *   "password": "YourSecurePassword"
 * }
 * </pre>
 */
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
