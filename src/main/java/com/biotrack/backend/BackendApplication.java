package com.biotrack.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//fix
@SpringBootApplication
public class BackendApplication {

    public static void main(String[] args) {
        // Cargar variables de entorno desde .env
        try {
            java.nio.file.Path envPath = java.nio.file.Paths.get(".env");
            if (java.nio.file.Files.exists(envPath)) {
                java.util.Properties props = new java.util.Properties();
                try (java.io.FileInputStream fis = new java.io.FileInputStream(".env")) {
                    props.load(fis);
                    props.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load .env file: " + e.getMessage());
        }
        
        SpringApplication.run(BackendApplication.class, args);
    }
}
