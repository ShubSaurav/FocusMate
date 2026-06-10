package com.focusmate.db;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.Statement;

@Component
public class SchemaInitializer implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        try (Connection c = DB.get(); Statement st = c.createStatement()) {
            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "email VARCHAR(255) NOT NULL UNIQUE, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "priority INT NOT NULL DEFAULT 0, " +
                    "due_date DATE NULL, " +
                    "target_minutes INT NOT NULL DEFAULT 0, " +
                    "status VARCHAR(32) NOT NULL DEFAULT 'PENDING', " +
                    "user_id INT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS sessions (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "task_id INT NULL, " +
                    "user_id INT NULL, " +
                    "start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "end_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "planned_minutes INT NOT NULL, " +
                    "actual_minutes INT NOT NULL, " +
                    "stopped_manually TINYINT(1) NOT NULL DEFAULT 0, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "CONSTRAINT fk_sessions_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE SET NULL" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            st.executeUpdate(
                "CREATE TABLE IF NOT EXISTS presets (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "focus_min INT NOT NULL DEFAULT 25, " +
                    "short_break_min INT NOT NULL DEFAULT 5, " +
                    "long_break_min INT NOT NULL DEFAULT 15, " +
                    "cycles_before_long INT NOT NULL DEFAULT 4" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            System.out.println("✅ Database schema initialized successfully.");
        } catch (Exception e) {
            System.err.println("⚠️ Database schema initialization failed (app will use in-memory fallback): " + e.getMessage());
        }
    }
}
