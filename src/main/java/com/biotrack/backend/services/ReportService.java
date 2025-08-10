package com.biotrack.backend.services;

import com.biotrack.backend.dto.PatientReportsDTO;
import com.biotrack.backend.dto.PatientFriendlyReportResponseDTO;
import com.biotrack.backend.dto.MedicalStudyReportResponseDTO;
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
    List<PatientReportsDTO> getPatientReports(UUID patientId);
    Object getReportFromS3(String s3Url, boolean isPatientFriendly);
}