package com.focusmate.db;

import java.sql.Connection;
import java.sql.DriverManager;

public class DB {
    private static final String URL = "jdbc:mysql://localhost:3306/focus_mat?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";  // change if needed
    private static final String PASS = "MARS*1979";  // change to your MySQL password

    public static Connection get() throws Exception {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}