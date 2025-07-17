package com.biotrack.backend.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Application health checks")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @GetMapping("/database")
    @Operation(summary = "Check database connectivity", description = "Verify connection to PostgreSQL database")
    public Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            health.put("status", "UP");
            health.put("database", "PostgreSQL");
            health.put("url", datasourceUrl.replaceAll("password=[^&]*", "password=***"));
            health.put("driver", connection.getMetaData().getDriverName());
            health.put("version", connection.getMetaData().getDatabaseProductVersion());
            health.put("connection_valid", connection.isValid(5));
            
            // Test query
            var stmt = connection.createStatement();
            var rs = stmt.executeQuery("SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'public'");
            if (rs.next()) {
                health.put("tables_count", rs.getInt("table_count"));
            }
            
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("url", datasourceUrl.replaceAll("password=[^&]*", "password=***"));
        }
        
        return health;
    }

    @GetMapping("/aws")
    @Operation(summary = "Check AWS services", description = "Verify AWS services connectivity")
    public Map<String, Object> checkAWSHealth() {
        Map<String, Object> health = new HashMap<>();
        
        // Check if running on AWS RDS
        boolean isAWSRDS = datasourceUrl.contains("rds.amazonaws.com");
        health.put("using_aws_rds", isAWSRDS);
        
        if (isAWSRDS) {
            String region = extractRegionFromUrl(datasourceUrl);
            health.put("aws_region", region);
            health.put("rds_endpoint", extractEndpointFromUrl(datasourceUrl));
        }
        
        return health;
    }

    private String extractRegionFromUrl(String url) {
        try {
            // Extract region from RDS URL: cluster-abc123.us-east-1.rds.amazonaws.com
            String[] parts = url.split("\\.");
            for (int i = 0; i < parts.length - 3; i++) {
                if (parts[i + 2].equals("rds") && parts[i + 3].equals("amazonaws")) {
                    return parts[i + 1];
                }
            }
        } catch (Exception e) {
            return "unknown";
        }
        return "unknown";
    }

    private String extractEndpointFromUrl(String url) {
        try {
            // Extract from jdbc:postgresql://endpoint:5432/database
            return url.substring(url.indexOf("//") + 2, url.lastIndexOf(":"));
        } catch (Exception e) {
            return "unknown";
        }
    }
}