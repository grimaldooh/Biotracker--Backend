package com.biotrack.backend.services;

import com.biotrack.backend.models.Patient;

public interface EmailService {
    void sendReportNotificationEmail(Patient patient, String specialistRecommendation, String specialistType, boolean needsSpecialist);
}