package com.biotrack.backend.controllers;

import com.biotrack.backend.services.SmsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final SmsService smsService;

    public TestController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/sms")
    public ResponseEntity<String> testSms(@RequestParam String phone, @RequestParam String name) {
        try {
            smsService.sendReportGeneratedNotification(phone, name, "reporte de prueba");
            return ResponseEntity.ok("SMS sent successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}