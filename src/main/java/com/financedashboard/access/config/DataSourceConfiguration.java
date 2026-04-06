package com.financedashboard.access.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

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
        try {
            URI databaseUri = new URI(renderDatabaseUrl);
            String[] credentials = databaseUri.getUserInfo().split(":", 2);
            String jdbcUrl = "jdbc:postgresql://%s:%d%s".formatted(
                    databaseUri.getHost(),
                    databaseUri.getPort(),
                    databaseUri.getPath()
            );

            HikariDataSource dataSource = new HikariDataSource();
            dataSource.setJdbcUrl(jdbcUrl);
            dataSource.setDriverClassName("org.postgresql.Driver");
            dataSource.setUsername(credentials[0]);
            dataSource.setPassword(credentials.length > 1 ? credentials[1] : "");
            return dataSource;
        } catch (URISyntaxException exception) {
            throw new IllegalStateException("DATABASE_URL is not a valid URI", exception);
        }
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
