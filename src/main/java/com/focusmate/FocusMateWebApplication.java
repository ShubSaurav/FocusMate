package com.focusmate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class FocusMateWebApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(FocusMateWebApplication.class, args);
        Environment env = ctx.getEnvironment();

        String host = env.getProperty("server.address", "localhost");
        String port = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));

        System.out.println("\n🎯 FocusMate Web Application Started!");
        System.out.println("📱 Open: http://" + host + ":" + port);
        System.out.println("⏱️  Pomodoro Timer | 📋 Task Scheduler | 📊 Analytics\n");
    }
}
