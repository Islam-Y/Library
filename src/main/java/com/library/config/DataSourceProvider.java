package com.library.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DataSourceProvider {
    private static final String PROPERTIES_FILE = "application.properties";
    private static HikariDataSource dataSource;

    static {
        try (InputStream input = DataSourceProvider.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("Не найден файл конфигурации: " + PROPERTIES_FILE);
            }

            Properties properties = new Properties();
            properties.load(input);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.user"));
            config.setPassword(properties.getProperty("db.password"));
            config.setDriverClassName(properties.getProperty("db.driver"));

            // Настройки пула
            config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.pool.size", "10")));
            config.setMinimumIdle(Integer.parseInt(properties.getProperty("db.pool.minIdle", "2")));
            config.setIdleTimeout(30000);
            config.setMaxLifetime(1800000);
            config.setConnectionTimeout(10000);
            config.setPoolName("LibraryHikariPool");

            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки конфигурации", e);
        }
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
