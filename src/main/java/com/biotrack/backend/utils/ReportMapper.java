package com.biotrack.backend.utils;

import com.biotrack.backend.dto.ReportDTO;
import com.biotrack.backend.models.Report;

public class ReportMapper {
    
    public static ReportDTO toDTO(Report report) {
        return new ReportDTO(
                report.getId(),
                report.getSample() != null ? report.getSample().getId() : null,
                report.getS3Url(),
                report.getS3UrlPatient(),
                report.getGeneratedAt(),
                report.getOpenaiModel(),
                report.getStatus(),
                report.getProcessingTimeMs()
        );
    }
    
    // Note: toEntity no es necesario ya que los reportes se construyen programáticamente
    // y no desde input del usuario
}