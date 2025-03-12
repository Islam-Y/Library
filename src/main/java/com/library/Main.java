package com.library;

import com.library.config.DatabaseConfig;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        DataSource dataSource = DatabaseConfig.getDataSource();
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Подключение к БД успешно!");

            // Пример выполнения простого SQL-запроса
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 1");
                if (rs.next()) {
                    System.out.println("Результат тестового запроса: " + rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Ошибка подключения: " + e.getMessage());
        }
    }

}