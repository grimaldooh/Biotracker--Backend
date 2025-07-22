package com.biotrack.backend.services;

import com.biotrack.backend.models.Report;
import com.biotrack.backend.models.enums.ReportStatus;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    Report generateReport(UUID sampleId);
    Report generateReportWithPatientInfo(UUID sampleId, String patientInfo);
    List<Report> findBySampleId(UUID sampleId);
    List<Report> findByStatus(ReportStatus status);
    Report findById(UUID reportId);
    void deleteReport(UUID reportId);
    boolean hasCompletedReport(UUID sampleId);
    Report getLatestCompletedReport(UUID sampleId);
    List<Report> findAll();
    Report generateClinicalReport(UUID sampleId);
}