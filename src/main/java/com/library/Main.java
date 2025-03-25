package com.library;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/library_db";
        String user = "library_app";
        String password = "1395";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT version()")) {

            System.out.println("Подключение успешно!");
            while (rs.next()) {
                System.out.println("Версия PostgreSQL: " + rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        logger.info("Тест логгера: сообщение INFO");
        logger.debug("Тест логгера: сообщение DEBUG");
    }
}