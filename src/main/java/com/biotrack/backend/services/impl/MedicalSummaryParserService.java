package com.biotrack.backend.services.impl;

import com.biotrack.backend.dto.medical.MedicalReportSummaryDTO;
import com.biotrack.backend.dto.insurance.MedicalDataForInsuranceDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MedicalSummaryParserService {

    private final ObjectMapper objectMapper;

    public MedicalDataForInsuranceDTO parseMedicalSummary(String jsonSummary) {
        try {
            MedicalReportSummaryDTO summary = objectMapper.readValue(jsonSummary, MedicalReportSummaryDTO.class);
            return extractInsuranceRelevantData(summary);
        } catch (Exception e) {
            log.error("Error parsing medical summary JSON", e);
            return createDefaultMedicalData();
        }
    }

    private MedicalDataForInsuranceDTO extractInsuranceRelevantData(MedicalReportSummaryDTO summary) {
        MedicalReportSummaryDTO.MedicalReport report = summary.getReporteMedico();
        
        return MedicalDataForInsuranceDTO.builder()
                .age(calculateAge(report.getPaciente().getFechaNacimiento()))
                .chronicConditionsCount(countChronicConditions(report.getResumen().getEnfermedadesDetectadas()))
                .consultationsThisYear(countRecentConsultations(report.getHistorialMedico()))
                .medicationsCount(estimateMedicationsFromHistory(report.getHistorialMedico()))
                .recentStudiesCount(report.getReportesEstudiosRecientes() != null ? 
                    report.getReportesEstudiosRecientes().size() : 0)
                .hasAbnormalLabResults(hasAbnormalLabResults(report.getReportesEstudiosRecientes()))
                .riskFactorsFromHistory(extractRiskFactors(report.getHistorialMedico()))
                .build();
    }

    private Integer calculateAge(String fechaNacimiento) {
        if (fechaNacimiento == null || fechaNacimiento.trim().isEmpty()) {
            return 35; // Edad por defecto
        }
        
        try {
            LocalDate birthDate = LocalDate.parse(fechaNacimiento);
            return LocalDate.now().getYear() - birthDate.getYear();
        } catch (DateTimeParseException e) {
            log.warn("Could not parse birth date: {}", fechaNacimiento);
            return 35;
        }
    }

    private Integer countChronicConditions(List<String> enfermedadesDetectadas) {
        if (enfermedadesDetectadas == null) {
            return 0;
        }
        
        // Filtrar condiciones que se consideran crónicas
        return (int) enfermedadesDetectadas.stream()
                .filter(this::isChronicCondition)
                .count();
    }

    private boolean isChronicCondition(String enfermedad) {
        String disease = enfermedad.toLowerCase();
        return disease.contains("diabetes") ||
               disease.contains("hipertension") ||
               disease.contains("cardio") ||
               disease.contains("renal") ||
               disease.contains("hepatic") ||
               disease.contains("pulmonar") ||
               disease.contains("cronic");
    }

    private Integer countRecentConsultations(List<MedicalReportSummaryDTO.MedicalReport.MedicalVisit> historial) {
        if (historial == null) {
            return 0;
        }
        
        LocalDateTime oneYearAgo = LocalDateTime.now().minusYears(1);
        
        return (int) historial.stream()
                .filter(visit -> {
                    try {
                        LocalDateTime visitDate = parseVisitDate(visit.getFechaVisita());
                        return visitDate.isAfter(oneYearAgo);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();
    }

    private LocalDateTime parseVisitDate(String fechaVisita) {
        try {
            // Formato esperado: "2025-08-18T18:32"
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
            return LocalDateTime.parse(fechaVisita, formatter);
        } catch (Exception e) {
            log.warn("Could not parse visit date: {}", fechaVisita);
            return LocalDateTime.now().minusMonths(6); // Fecha por defecto
        }
    }

    private Integer estimateMedicationsFromHistory(List<MedicalReportSummaryDTO.MedicalReport.MedicalVisit> historial) {
        if (historial == null || historial.isEmpty()) {
            return 0;
        }
        
        // Buscar menciones de medicamentos en las notas y diagnósticos
        return historial.stream()
                .mapToInt(this::countMedicationsInVisit)
                .max()
                .orElse(0);
    }

    private int countMedicationsInVisit(MedicalReportSummaryDTO.MedicalReport.MedicalVisit visit) {
        String text = (visit.getNotas() + " " + visit.getDiagnostico()).toLowerCase();
        
        int count = 0;
        if (text.contains("isotretinoina") || text.contains("isotretinoin")) count++;
        if (text.contains("suplemento") || text.contains("supplement")) count++;
        if (text.contains("proteina") || text.contains("protein")) count++;
        if (text.contains("creatina") || text.contains("creatine")) count++;
        if (text.contains("vitamina") || text.contains("vitamin")) count++;
        
        return count;
    }

    private Boolean hasAbnormalLabResults(List<MedicalReportSummaryDTO.MedicalReport.StudyReport> estudios) {
        if (estudios == null || estudios.isEmpty()) {
            return false;
        }
        
        return estudios.stream()
                .anyMatch(estudio -> {
                    String hallazgos = estudio.getHallazgosPrincipales().toLowerCase();
                    return hallazgos.contains("elevado") ||
                           hallazgos.contains("alto") ||
                           hallazgos.contains("anormal") ||
                           hallazgos.contains("alterado");
                });
    }

    private List<String> extractRiskFactors(List<MedicalReportSummaryDTO.MedicalReport.MedicalVisit> historial) {
        // Extraer factores de riesgo del historial médico
        return List.of(); // Implementar según necesidades específicas
    }

    private MedicalDataForInsuranceDTO createDefaultMedicalData() {
        return MedicalDataForInsuranceDTO.builder()
                .age(35)
                .chronicConditionsCount(0)
                .consultationsThisYear(1)
                .medicationsCount(0)
                .recentStudiesCount(0)
                .hasAbnormalLabResults(false)
                .riskFactorsFromHistory(List.of())
                .build();
    }
}