package com.library.config;

import com.library.exception.ConfigurationFileNotFoundException;
import com.library.exception.ConfigurationLoadException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DataSourceProvider {
    private static final String PROPERTIES_FILE = "application.properties";
    private static HikariDataSource dataSource;
    static ClassLoader classLoader = DataSourceProvider.class.getClassLoader();

    private DataSourceProvider() {
    }

    public static String getPropertiesFileName() {
        return PROPERTIES_FILE;
    }

    public static DataSource getDataSource() {
        if (dataSource == null) {
            initializeDataSource();
        }
        return dataSource;
    }

    private static synchronized void initializeDataSource() {
        if (dataSource == null) {
            String testing = System.getProperty("testing");
            if (testing != null && testing.equals("true")) {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(System.getProperty("db.url"));
                config.setUsername(System.getProperty("db.user"));
                config.setPassword(System.getProperty("db.password"));
                config.setDriverClassName("db.driver");
                dataSource = new HikariDataSource(config);
                return;
            }
            try (InputStream input = classLoader.getResourceAsStream(PROPERTIES_FILE)) {
                if (input == null) {
                    throw new ConfigurationFileNotFoundException("Не найден файл конфигурации: " + PROPERTIES_FILE);
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

                String initTimeout = properties.getProperty("db.initializationFailTimeout");
                if (initTimeout != null) {
                    config.setInitializationFailTimeout(Long.parseLong(initTimeout));
                }

                dataSource = new HikariDataSource(config);
            } catch (IOException e) {
                throw new ConfigurationLoadException("Ошибка загрузки конфигурации", e);
            }
        }
    }
}
