package com.biotrack.backend.controllers;

import com.biotrack.backend.services.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private final SmsService smsService;

    public TestController(SmsService smsService) {
        this.smsService = smsService;
        logger.info("TestController initialized with SmsService: {}", smsService.getClass().getSimpleName());
    }

    @PostMapping("/sms")
    public ResponseEntity<String> testSms(@RequestParam String phone, @RequestParam String name) {
        logger.info("Testing SMS to phone: {} for name: {}", phone, name);
        try {
            smsService.sendReportGeneratedNotification(phone, name, "reporte de prueba");
            return ResponseEntity.ok("SMS sent successfully to " + phone);
        } catch (Exception e) {
            logger.error("SMS test failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Alternativa con JSON
    @PostMapping("/sms-json")
    public ResponseEntity<String> testSmsJson(@RequestBody SmsTestRequest request) {
        logger.info("Testing SMS JSON to phone: {} for name: {}", request.getPhone(), request.getName());
        try {
            smsService.sendReportGeneratedNotification(request.getPhone(), request.getName(), "reporte de prueba");
            return ResponseEntity.ok("SMS sent successfully to " + request.getPhone());
        } catch (Exception e) {
            logger.error("SMS test failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // DTO para JSON
    public static class SmsTestRequest {
        private String phone;
        private String name;

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}