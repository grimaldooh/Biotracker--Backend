package com.biotrack.backend.services;

public interface SmsService {
    void sendReportGeneratedNotification(String phoneNumber, String patientName, String reportType);
    void sendAppointmentReminder(String phoneNumber, String patientName, String appointmentDate);
}