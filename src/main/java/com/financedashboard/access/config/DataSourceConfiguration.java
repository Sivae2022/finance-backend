package com.financedashboard.access.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    @Bean
    DataSource dataSource(Environment environment) {
        String configuredUrl = environment.getProperty("spring.datasource.url");
        if (configuredUrl != null && !configuredUrl.isBlank()) {
            return buildConfiguredDataSource(environment, configuredUrl);
        }

        String renderDatabaseUrl = environment.getProperty("DATABASE_URL");
        if (renderDatabaseUrl != null && !renderDatabaseUrl.isBlank()) {
            return buildRenderPostgresDataSource(renderDatabaseUrl);
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:file:./data/finance-access-db;AUTO_SERVER=TRUE");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }

    private DataSource buildConfiguredDataSource(Environment environment, String jdbcUrl) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbcUrl);
        dataSource.setDriverClassName(environment.getProperty("spring.datasource.driver-class-name", inferDriver(jdbcUrl)));
        dataSource.setUsername(environment.getProperty("spring.datasource.username", ""));
        dataSource.setPassword(environment.getProperty("spring.datasource.password", ""));
        return dataSource;
    }

    private DataSource buildRenderPostgresDataSource(String renderDatabaseUrl) {
        String normalized = renderDatabaseUrl.trim();
        String prefix = "postgresql://";
        if (!normalized.startsWith(prefix)) {
            throw new IllegalStateException("DATABASE_URL must start with postgresql://");
        }

        String remainder = normalized.substring(prefix.length());
        int atIndex = remainder.lastIndexOf('@');
        if (atIndex < 0) {
            throw new IllegalStateException("DATABASE_URL is missing credentials or host information");
        }

        String credentialsPart = remainder.substring(0, atIndex);
        String locationPart = remainder.substring(atIndex + 1);

        String[] credentials = credentialsPart.split(":", 2);
        String username = credentials[0];
        String password = credentials.length > 1 ? credentials[1] : "";

        int slashIndex = locationPart.indexOf('/');
        if (slashIndex < 0) {
            throw new IllegalStateException("DATABASE_URL is missing the database name");
        }

        String hostPort = locationPart.substring(0, slashIndex);
        String database = locationPart.substring(slashIndex);

        String host;
        int port = 5432;
        int colonIndex = hostPort.lastIndexOf(':');
        if (colonIndex >= 0) {
            host = hostPort.substring(0, colonIndex);
            String portValue = hostPort.substring(colonIndex + 1);
            if (!portValue.isBlank()) {
                port = Integer.parseInt(portValue);
            }
        } else {
            host = hostPort;
        }

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:postgresql://%s:%d%s".formatted(host, port, database));
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    private String inferDriver(String jdbcUrl) {
        if (jdbcUrl.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        }
        if (jdbcUrl.startsWith("jdbc:h2:")) {
            return "org.h2.Driver";
        }
        throw new IllegalStateException("Unable to infer driver class for datasource URL");
    }
}
