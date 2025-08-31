package com.biotrack.backend.services.impl;

import com.biotrack.backend.dto.PatientReportsDTO;
import com.biotrack.backend.dto.TechnicalGeneticReportDTO;
import com.biotrack.backend.dto.PatientFriendlyReportResponseDTO;
import com.biotrack.backend.dto.MedicalStudyReportResponseDTO;
import com.biotrack.backend.dto.PatientFriendlyGeneticReportDTO;
import com.biotrack.backend.dto.GeneticReportDTO;
import com.biotrack.backend.models.BloodSample;
import com.biotrack.backend.models.DnaSample;
import com.biotrack.backend.models.GeneticSample;
import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.Report;
import com.biotrack.backend.models.SalivaSample;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.models.enums.ReportStatus;
import com.biotrack.backend.repositories.MutationRepository;
import com.biotrack.backend.repositories.ReportRepository;
import com.biotrack.backend.services.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.biotrack.backend.services.EmailService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final MutationRepository mutationRepository;
    private final SampleService sampleService;
    private final OpenAIService openAIService;
    private final S3Service s3Service; // Para descargar contenido de S3
    private final PatientService patientService;
    private final GeneticSampleService geneticSampleService;
    private final ObjectMapper objectMapper; // Para parsear JSON
    private final EmailService emailService;

    public ReportServiceImpl(
            ReportRepository reportRepository,
            MutationRepository mutationRepository,
            SampleService sampleService,
            OpenAIService openAIService,
            S3Service s3Service,
            PatientService patientService,
            GeneticSampleService geneticSampleService,
            ObjectMapper objectMapper,
            EmailService emailService
    ) {
        this.reportRepository = reportRepository;
        this.mutationRepository = mutationRepository;
        this.sampleService = sampleService;
        this.openAIService = openAIService;
        this.s3Service = s3Service;
        this.patientService = patientService;
        this.geneticSampleService = geneticSampleService;
        this.objectMapper = objectMapper;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public Report generateReport(UUID sampleId) {
        return generateReportWithPatientInfo(sampleId, null);
    }

    @Override
    @Transactional
    public Report generateReportWithPatientInfo(UUID sampleId, String patientInfo) {
        // 1. Validaciones iniciales
        GeneticSample sample = geneticSampleService.findById(sampleId);
        
        if (!openAIService.isConfigured()) {
            throw new RuntimeException("OpenAI service is not configured. Please check API key configuration.");
        }

        // 2. Verificar si ya existe un reporte en proceso
        if (reportRepository.existsBySampleIdAndStatus(sampleId, ReportStatus.GENERATING)) {
            throw new RuntimeException("A report is already being generated for this sample.");
        }

        // 3. Obtener mutaciones de la muestra
        List<Mutation> mutations = mutationRepository.findBySampleId(sampleId);
        if (mutations.isEmpty()) {
            throw new RuntimeException("No mutations found for sample. Process the result file first.");
        }

        long startTime = System.currentTimeMillis();

        // 4. Crear reporte inicial con estado GENERATING
        Report report = Report.builder()
                .geneticSample(sample)
                .status(ReportStatus.GENERATING)
                .generatedAt(LocalDateTime.now())
                .openaiModel(openAIService.getModelUsed())
                .build();
        
        report = reportRepository.save(report);

        try {
            // 5. Construir información del paciente y contexto clínico
            String patientContext = buildPatientContext(sample);
            String patientClinicalSummary = patientService.getLatestSummaryText(sample.getPatient().getId());
            
            // 6. Generar AMBOS reportes con OpenAI
            // Reporte técnico genético
            String technicalReportContent = openAIService.generateGeneticReport(mutations, patientClinicalSummary);
            
            // Reporte genético patient-friendly
            String patientFriendlyReportContent = openAIService.generatePatientFriendlyGeneticReport(mutations, patientClinicalSummary, technicalReportContent);

            // 7. Subir AMBOS reportes a S3
            String technicalS3Key = generateReportS3Key(report.getId());
            String patientFriendlyS3Key = generatePatientGeneticReportS3Key(report.getId());
            
            String technicalS3Url = s3Service.uploadTextContent(technicalReportContent, technicalS3Key);
            String patientFriendlyS3Url = s3Service.uploadTextContent(patientFriendlyReportContent, patientFriendlyS3Key);

            // 8. Calcular tiempo de procesamiento
            long processingTime = System.currentTimeMillis() - startTime;

            // 9. Actualizar reporte con AMBOS archivos
            report.setS3Key(technicalS3Key);
            report.setS3Url(technicalS3Url);
            report.setS3KeyPatient(patientFriendlyS3Key);
            report.setS3UrlPatient(patientFriendlyS3Url);
            report.setFileSize((long) technicalReportContent.getBytes().length);
            report.setProcessingTimeMs(processingTime);
            report.setStatus(ReportStatus.COMPLETED);

            return reportRepository.save(report);

        } catch (Exception e) {
            // 10. Marcar como fallido en caso de error
            report.setStatus(ReportStatus.FAILED);
            report.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            reportRepository.save(report);
            
            throw new RuntimeException("Error generating genetic report: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Report generateClinicalReport(UUID sampleId) {
        Sample sample = sampleService.findById(sampleId);

        if (!openAIService.isConfigured()) {
            throw new RuntimeException("OpenAI service is not configured. Please check API key configuration.");
        }

        long startTime = System.currentTimeMillis();

        // Crear reporte inicial
        Report report = Report.builder()
                .sample(sample)
                .status(ReportStatus.GENERATING)
                .generatedAt(LocalDateTime.now())
                .openaiModel(openAIService.getModelUsed())
                .build();

        report = reportRepository.save(report);

        try {
            // Construir contexto clínico completo
            String patientContext = buildPatientContext(sample);

            // Puedes agregar aquí los datos específicos de la muestra (blood, dna, saliva)
            String sampleInfo = sample.getSpecificSampleInfo();
            String sampleTypeInfo = sample.getSpecificSampleTypeInfo();
            
            String fullContext = patientContext + "\n" + sampleInfo + "\n" + sampleTypeInfo;

            // Generar AMBOS reportes con OpenAI
            String clinicalReportContent = openAIService.generateClinicalReport(fullContext);
            String patientFriendlyReportContent = openAIService.generatePatientFriendlyClinicalReport(fullContext);

            // Subir AMBOS reportes a S3
            String s3Key = generateReportS3Key(report.getId());
            String s3KeyPatient = generatePatientReportS3Key(report.getId());
            
            String s3Url = s3Service.uploadTextContent(clinicalReportContent, s3Key);
            String s3UrlPatient = s3Service.uploadTextContent(patientFriendlyReportContent, s3KeyPatient);

            long processingTime = System.currentTimeMillis() - startTime;

            // Actualizar reporte con AMBOS archivos
            report.setS3Key(s3Key);
            report.setS3Url(s3Url);
            report.setS3KeyPatient(s3KeyPatient);
            report.setS3UrlPatient(s3UrlPatient);
            report.setFileSize((long) clinicalReportContent.getBytes().length);
            report.setProcessingTimeMs(processingTime);
            report.setStatus(ReportStatus.COMPLETED);

            sendReportNotificationWithSpecialistRecommendation(sample, patientFriendlyReportContent);

            return reportRepository.save(report);

        } catch (Exception e) {
            report.setStatus(ReportStatus.FAILED);
            report.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            reportRepository.save(report);

            throw new RuntimeException("Error generating clinical report: " + e.getMessage(), e);
        }
    }

    private void sendReportNotificationWithSpecialistRecommendation(Sample sample, String patientFriendlyReportContent) {
    try {
        // Solo enviar recomendación si la muestra NO tiene doctor referido
        if (sample.getDoctorReferedId() == null) {
            // Parsear el JSON del reporte para extraer la recomendación de IA
            String cleanedContent = cleanContentForParsing(patientFriendlyReportContent);
            
            if (cleanedContent.startsWith("{")) {
                JsonNode reportJson = objectMapper.readTree(cleanedContent);
                JsonNode aiRecommendation = reportJson.path("patient_friendly_report").path("ai_recommendation");
                
                boolean specialistNeeded = aiRecommendation.path("specialist_needed").asBoolean(false);
                String specialistType = aiRecommendation.path("specialist_type").isNull() ? 
                    null : aiRecommendation.path("specialist_type").asText();
                String reason = aiRecommendation.path("reason").isNull() ? 
                    null : aiRecommendation.path("reason").asText();
                
                // Enviar email con la recomendación
                if (sample.getPatient() != null && sample.getPatient().getEmail() != null) {
                    emailService.sendReportNotificationEmail(
                        sample.getPatient(),
                        reason,
                        specialistType,
                        specialistNeeded
                    );
                }
            }
        }
        // Si tiene doctor referido, no se envía recomendación (el paciente volverá con su médico)
        
    } catch (Exception e) {
        // Log error pero no fallar el proceso principal
        System.err.println("Warning: Could not send specialist recommendation email: " + e.getMessage());
    }
}

    @Override
    public List<Report> findBySampleId(UUID sampleId) {
        return reportRepository.findBySampleIdOrderByGeneratedAtDesc(sampleId);
    }

    @Override
    public List<Report> findByStatus(ReportStatus status) {
        return reportRepository.findByStatusOrderByGeneratedAtDesc(status);
    }

    @Override
    public List<Report> findAll(){
        return reportRepository.findAll();
    }


    @Override
    public Report findById(UUID reportId) {
        return reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + reportId));
    }

    @Override
    @Transactional
    public void deleteReport(UUID reportId) {
        Report report = findById(reportId);
        
        // Eliminar archivo técnico de S3 si existe
        if (report.getS3Key() != null) {
            try {
                s3Service.deleteFile(report.getS3Key());
            } catch (Exception e) {
                System.err.println("Warning: Could not delete technical report file from S3: " + e.getMessage());
            }
        }
        
        // Eliminar archivo del paciente de S3 si existe
        if (report.getS3KeyPatient() != null) {
            try {
                s3Service.deleteFile(report.getS3KeyPatient());
            } catch (Exception e) {
                System.err.println("Warning: Could not delete patient report file from S3: " + e.getMessage());
            }
        }
        
        // Eliminar de base de datos
        reportRepository.delete(report);
    }

    @Override
    public boolean hasCompletedReport(UUID sampleId) {
        return reportRepository.findLatestCompletedBySampleId(sampleId).isPresent();
    }

    @Override
    public Report getLatestCompletedReport(UUID sampleId) {
        return reportRepository.findLatestCompletedBySampleId(sampleId)
                .orElseThrow(() -> new RuntimeException("No completed report found for sample: " + sampleId));
    }

    /**
     * Construye el contexto del paciente para el prompt
     */
    private String buildPatientContext(Sample sample) {
        StringBuilder context = new StringBuilder();
        
        if (sample.getPatient() != null) {
            context.append("Patient Information:\n");
            context.append("- Name: ").append(sample.getPatient().getFirstName())
                   .append(" ").append(sample.getPatient().getLastName()).append("\n");
            context.append("- Gender: ").append(sample.getPatient().getGender()).append("\n");
            context.append("- Birth Date: ").append(sample.getPatient().getBirthDate()).append("\n");
            
            if (sample.getPatient().getCurp() != null) {
                context.append("- CURP: ").append(sample.getPatient().getCurp()).append("\n");
            }
        }
        
        context.append("Sample Information:\n");
        context.append("- Sample Type: ").append(sample.getType()).append("\n");
        context.append("- Collection Date: ").append(sample.getCollectionDate()).append("\n");
        context.append("- Status: ").append(sample.getStatus()).append("\n");
        
        if (sample.getNotes() != null && !sample.getNotes().trim().isEmpty()) {
            context.append("- Clinical Notes: ").append(sample.getNotes()).append("\n");
        }
        
        // if (additionalInfo != null && !additionalInfo.trim().isEmpty()) {
        //     context.append("\nAdditional Clinical Information:\n");
        //     context.append(additionalInfo);
        // }
        
        return context.toString();
    }

    private String buildPatientContext(GeneticSample sample) {
        StringBuilder context = new StringBuilder();
        
        if (sample.getPatient() != null) {
            context.append("Patient Information:\n");
            context.append("- Name: ").append(sample.getPatient().getFirstName())
                   .append(" ").append(sample.getPatient().getLastName()).append("\n");
            context.append("- Gender: ").append(sample.getPatient().getGender()).append("\n");
            context.append("- Birth Date: ").append(sample.getPatient().getBirthDate()).append("\n");
            
            if (sample.getPatient().getCurp() != null) {
                context.append("- CURP: ").append(sample.getPatient().getCurp()).append("\n");
            }
        }
        
        context.append("Sample Information:\n");
        context.append("- Sample Type: ").append(sample.getType()).append("\n");
        context.append("- Collection Date: ").append(sample.getCollectionDate()).append("\n");
        context.append("- Status: ").append(sample.getStatus()).append("\n");
        
        if (sample.getNotes() != null && !sample.getNotes().trim().isEmpty()) {
            context.append("- Clinical Notes: ").append(sample.getNotes()).append("\n");
        }
        
        // if (additionalInfo != null && !additionalInfo.trim().isEmpty()) {
        //     context.append("\nAdditional Clinical Information:\n");
        //     context.append(additionalInfo);
        // }
        
        return context.toString();
    }

    /**
     * Genera el key único para el reporte en S3
     */
    private String generateReportS3Key(UUID reportId) {
        long timestamp = System.currentTimeMillis();
        return String.format("reports/%d_%s_genetic_report.txt", timestamp, reportId.toString());
    }

    /**
     * Genera el key único para el reporte del paciente en S3
     */
    private String generatePatientReportS3Key(UUID reportId) {
        long timestamp = System.currentTimeMillis();
        return String.format("reports/%d_%s_patient_friendly_report.txt", timestamp, reportId.toString());
    }

    // ✅ NUEVO: Método para generar clave S3 para reporte genético patient-friendly
    private String generatePatientGeneticReportS3Key(UUID reportId) {
        long timestamp = System.currentTimeMillis();
        return String.format("reports/%d_%s_genetic_patient_friendly.json", timestamp, reportId.toString());
    }

    @Override
    public List<PatientReportsDTO> getPatientReports(UUID patientId) {
        // 1. Validar que el paciente existe
        patientService.findById(patientId);
        
        // 2. Obtener todos los reportes del paciente
        List<Report> reports = reportRepository.findReportsByPatientId(patientId);
        
        // 3. Agrupar reportes por sample_id y combinar URLs
        Map<UUID, PatientReportsDTO> reportsMap = reports.stream()
            .filter(report -> report.getSample() != null)
            .collect(Collectors.toMap(
                report -> report.getSample().getId(),
                this::mapToPatientReportsDTO,
                this::mergeReports // Función para combinar reportes del mismo sample
            ));
        
        // 4. Retornar como lista ordenada por ID de muestra
        return reportsMap.values().stream()
            .sorted((a, b) -> a.sampleId().compareTo(b.sampleId()))
            .toList();
    }
    
    private PatientReportsDTO mapToPatientReportsDTO(Report report) {
        return new PatientReportsDTO(
            report.getSample().getId(),
            report.getS3Url(),
            report.getS3UrlPatient()
        );
    }
    
    private PatientReportsDTO mergeReports(PatientReportsDTO existing, PatientReportsDTO newReport) {
        return new PatientReportsDTO(
            existing.sampleId(),
            existing.s3Url() != null ? existing.s3Url() : newReport.s3Url(),
            existing.s3UrlPatient() != null ? existing.s3UrlPatient() : newReport.s3UrlPatient()
        );
    }

    private String cleanContentForParsing(String content) {
    if (content == null) {
        return null;
    }
    
    // Remover BOM UTF-8 (0xFEFF)
    if (content.startsWith("\uFEFF")) {
        content = content.substring(1);
    }
    
    // Remover otros caracteres invisibles comunes al inicio
    content = content.replaceAll("^[\u200B\u200C\u200D\uFEFF]+", "");
    
    // Intentar corregir problemas de codificación UTF-8 comunes
    content = fixUtf8Encoding(content);
    
    // Trim espacios normales
    content = content.trim();
    
    return content;
}

/**
 * Corrige problemas comunes de codificación UTF-8
 */
private String fixUtf8Encoding(String content) {
    if (content == null) {
        return null;
    }
    
    try {
        // Verificar si el contenido tiene problemas de codificación
        if (content.contains("Ã¡") || content.contains("Ã³") || content.contains("Ã©") || 
            content.contains("Ã­") || content.contains("Ãº") || content.contains("Ã±")) {
            
            // Convertir a bytes usando ISO-8859-1 y luego a UTF-8
            byte[] bytes = content.getBytes("ISO-8859-1");
            content = new String(bytes, "UTF-8");
        }
    } catch (Exception e) {
        // Si hay error en la conversión, mantener el contenido original
        System.err.println("Warning: Could not fix UTF-8 encoding: " + e.getMessage());
    }
    
    return content;
}

@Override
public Object getReportFromS3(String s3Url, boolean isPatientFriendly) {
    try {
        // 1. Validar URL de S3
        if (s3Url == null || s3Url.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 URL cannot be null or empty");
        }

        // 2. Descargar contenido del archivo desde S3
        String reportContent = s3Service.downloadFileAsString(s3Url);

        if (reportContent == null || reportContent.trim().isEmpty()) {
            throw new RuntimeException("Report content is empty or could not be downloaded from S3");
        }

        // 3. Limpiar BOM, caracteres invisibles y problemas de codificación
        String cleanedContent = cleanContentForParsing(reportContent);

        // 4. Verificar si el contenido es JSON o texto plano
        if (cleanedContent.startsWith("{")) {
            // Es JSON - parsear según el tipo de reporte
            if (isPatientFriendly) {
                return objectMapper.readValue(cleanedContent, PatientFriendlyReportResponseDTO.class);
            } else {
                return objectMapper.readValue(cleanedContent, MedicalStudyReportResponseDTO.class);
            }
        } else {
            // Es texto plano - retornar como string directamente
            return cleanedContent;
        }

    } catch (Exception e) {
        throw new RuntimeException("Error processing report from S3: " + e.getMessage(), e);
    }
}

@Override
public List<GeneticReportDTO> getGeneticReportsByPatient(UUID patientId) {
    return reportRepository.findGeneticReportsByPatientId(patientId)
        .stream()
        .map(r -> new GeneticReportDTO(
            r.getId(),
            r.getGeneticSample() == null ? null : new GeneticReportDTO.GeneticSampleInfo(
                r.getGeneticSample().getId(),
                r.getGeneticSample().getType(),
                r.getGeneticSample().getStatus(),
                r.getGeneticSample().getMedicalEntityId(),
                r.getGeneticSample().getCollectionDate(),
                r.getGeneticSample().getNotes(),
                r.getGeneticSample().getCreatedAt(),
                r.getGeneticSample().getConfidenceScore(),
                r.getGeneticSample().getProcessingSoftware(),
                r.getGeneticSample().getReferenceGenome()
            ),
            r.getSample() == null ? null : new GeneticReportDTO.SampleInfo(
                r.getSample().getId(),
                r.getSample().getType(),
                r.getSample().getStatus(),
                r.getSample().getMedicalEntityId(),
                r.getSample().getCollectionDate(),
                r.getSample().getNotes(),
                r.getSample().getCreatedAt()
            ),
            r.getS3Url(),
            r.getS3UrlPatient()
        ))
        .toList();
}

@Override
public Object getGeneticReportFromUrl(String s3Url, boolean isPatientFriendly) {
    try {
        // 1. Validar URL de S3
        if (s3Url == null || s3Url.trim().isEmpty()) {
            throw new IllegalArgumentException("S3 URL cannot be null or empty");
        }
        
        // 2. Descargar contenido del archivo desde S3
        String reportContent = s3Service.downloadFileAsStringNotFormated(s3Url);
        
        if (reportContent == null || reportContent.trim().isEmpty()) {
            throw new RuntimeException("Report content is empty or could not be downloaded from S3");
        }
        
        // 3. Limpiar BOM, caracteres invisibles y problemas de codificación
        String cleanedContent = cleanContentForParsing(reportContent);
        
        // 4. Parsear JSON según el tipo de reporte
        if (isPatientFriendly) {
            return objectMapper.readValue(cleanedContent, PatientFriendlyGeneticReportDTO.class);
        } else {
            return objectMapper.readValue(cleanedContent, TechnicalGeneticReportDTO.class);
        }
        
    } catch (Exception e) {
        throw new RuntimeException("Error processing genetic report from S3: " + e.getMessage(), e);
    }
}
}