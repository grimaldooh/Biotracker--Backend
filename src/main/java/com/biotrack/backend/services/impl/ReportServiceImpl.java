package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.BloodSample;
import com.biotrack.backend.models.DnaSample;
import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.Report;
import com.biotrack.backend.models.SalivaSample;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.models.enums.ReportStatus;
import com.biotrack.backend.repositories.MutationRepository;
import com.biotrack.backend.repositories.ReportRepository;
import com.biotrack.backend.services.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final MutationRepository mutationRepository;
    private final SampleService sampleService;
    private final OpenAIService openAIService;
    private final S3Service s3Service;

    public ReportServiceImpl(
            ReportRepository reportRepository,
            MutationRepository mutationRepository,
            SampleService sampleService,
            OpenAIService openAIService,
            S3Service s3Service
    ) {
        this.reportRepository = reportRepository;
        this.mutationRepository = mutationRepository;
        this.sampleService = sampleService;
        this.openAIService = openAIService;
        this.s3Service = s3Service;
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
        Sample sample = sampleService.findById(sampleId);
        
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
                .sample(sample)
                .status(ReportStatus.GENERATING)
                .generatedAt(LocalDateTime.now())
                .openaiModel(openAIService.getModelUsed())
                .build();
        
        report = reportRepository.save(report);

        try {
            // 5. Construir información del paciente si no se proporciona
            String patientContext = buildPatientContext(sample);

            // 6. Generar reporte con OpenAI
            String reportContent = openAIService.generateGeneticReport(mutations, patientContext);

            // 7. Subir reporte a S3
            String s3Key = generateReportS3Key(report.getId());
            String s3Url = s3Service.uploadTextContent(reportContent, s3Key);

            // 8. Calcular tiempo de procesamiento
            long processingTime = System.currentTimeMillis() - startTime;

            // 9. Actualizar reporte con datos finales
            report.setS3Key(s3Key);
            report.setS3Url(s3Url);
            report.setFileSize((long) reportContent.getBytes().length);
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

            // Generar reporte clínico con OpenAI (sin mutaciones)
            String reportContent = openAIService.generateClinicalReport(fullContext);

            // Subir reporte a S3
            String s3Key = generateReportS3Key(report.getId());
            String s3Url = s3Service.uploadTextContent(reportContent, s3Key);

            long processingTime = System.currentTimeMillis() - startTime;

            report.setS3Key(s3Key);
            report.setS3Url(s3Url);
            report.setFileSize((long) reportContent.getBytes().length);
            report.setProcessingTimeMs(processingTime);
            report.setStatus(ReportStatus.COMPLETED);

            return reportRepository.save(report);

        } catch (Exception e) {
            report.setStatus(ReportStatus.FAILED);
            report.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            reportRepository.save(report);

            throw new RuntimeException("Error generating clinical report: " + e.getMessage(), e);
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
        
        // Eliminar archivo de S3 si existe
        if (report.getS3Key() != null) {
            try {
                s3Service.deleteFile(report.getS3Key());
            } catch (Exception e) {
                // Log pero no fallar si el archivo no existe en S3
                System.err.println("Warning: Could not delete report file from S3: " + e.getMessage());
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

    /**
     * Genera el key único para el reporte en S3
     */
    private String generateReportS3Key(UUID reportId) {
        long timestamp = System.currentTimeMillis();
        return String.format("reports/%d_%s_genetic_report.txt", timestamp, reportId.toString());
    }
}