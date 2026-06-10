package com.focusmate.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/focusmate?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "MARS*1979";

    public static Connection get() throws Exception {
        String envUrl = System.getenv("SPRING_DATASOURCE_URL");
        String envUser = System.getenv("SPRING_DATASOURCE_USERNAME");
        String envPass = System.getenv("SPRING_DATASOURCE_PASSWORD");

        String url = (envUrl != null && !envUrl.isBlank()) ? envUrl : DEFAULT_URL;
        String user = (envUser != null && !envUser.isBlank()) ? envUser : DEFAULT_USER;
        String pass = (envPass != null) ? envPass : DEFAULT_PASS;

        return DriverManager.getConnection(url, user, pass);
    }
}