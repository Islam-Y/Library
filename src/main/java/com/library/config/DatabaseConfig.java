package com.library.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final String PROPERTIES_FILE = "application.properties";
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException("Не найден файл конфигурации: " + PROPERTIES_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки конфигурации", e);
        }
    }

    public static DataSource getDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(properties.getProperty("db.url"));
        config.setUsername(properties.getProperty("db.user"));
        config.setPassword(properties.getProperty("db.password"));
        config.setDriverClassName(properties.getProperty("db.driver"));

        // Настройки пула (опционально)
        config.setMaximumPoolSize(Integer.parseInt(properties.getProperty("db.pool.size", "10")));
        config.setMinimumIdle(Integer.parseInt(properties.getProperty("db.pool.minIdle", "2")));  // Минимальное количество соединений в режиме простоя
        config.setIdleTimeout(30000); // Время простоя соединения (30 секунд)
        config.setMaxLifetime(1800000); // Максимальное время жизни соединения (30 минут)
        config.setConnectionTimeout(10000); // Таймаут ожидания соединения (10 секунд)
        config.setPoolName("LibraryHikariPool"); // Имя пула

        return new HikariDataSource(config);
    }
}
