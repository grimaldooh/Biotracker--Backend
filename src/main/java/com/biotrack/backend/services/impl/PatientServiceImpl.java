package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.ClinicalHistoryRecord;
import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.Report;
import com.biotrack.backend.repositories.ClinicalHistoryRecordRepository;
import com.biotrack.backend.repositories.MedicalVisitRepository;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.repositories.ReportRepository;
import com.biotrack.backend.services.OpenAIService;
import com.biotrack.backend.services.PatientService;
import com.biotrack.backend.services.S3Service;
import com.biotrack.backend.services.aws.S3ServiceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final MedicalVisitRepository medicalVisitRepository;
    private final ReportRepository reportRepository;
    private final S3ServiceImpl s3Service;
    private final OpenAIServiceImpl openAIService; 
    private final ClinicalHistoryRecordRepository clinicalHistoryRecordRepository;

    public PatientServiceImpl(PatientRepository patientRepository, 
                              MedicalVisitRepository medicalVisitRepository,
                              ReportRepository reportRepository,
                              S3ServiceImpl s3Service,
                              OpenAIServiceImpl openAIService,
                              ClinicalHistoryRecordRepository clinicalHistoryRecordRepository) {
        this.patientRepository = patientRepository;
        this.medicalVisitRepository = medicalVisitRepository;
        this.reportRepository = reportRepository;
        this.s3Service = s3Service;
        this.openAIService = openAIService;
        this.clinicalHistoryRecordRepository = clinicalHistoryRecordRepository;
    }

    @Override
    public Patient create(Patient patient){
        return patientRepository.save(patient);
    }

    @Override
    public List<Patient> findAll(){
        return patientRepository.findAll();
    }

    @Override
    public Patient findById(UUID id){
        return patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    @Override
    @Transactional
    public Patient update(UUID id , Patient patient){
        Patient existing = patientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        existing.setFirstName(patient.getFirstName());
        existing.setLastName(patient.getLastName());
        existing.setGender(patient.getGender());
        existing.setBirthDate(patient.getBirthDate());
        existing.setCurp(patient.getCurp());
        return patientRepository.save(existing);
    }


    @Override
    @Transactional
    public void deleteById(UUID id){
        patientRepository.deleteById(id);
    }

    @Override
    @Transactional
    public ClinicalHistoryRecord generatePatientClinicalSummary(UUID patientId) {
    Patient patient = findById(patientId);

    // 1. Obtener historial de visitas médicas
    List<MedicalVisit> visits = medicalVisitRepository.findByPatientId(patientId);

    // 2. Obtener últimos 5 reportes de estudios
    List<Report> reports = reportRepository.findByPatientIdOrderByGeneratedAtDesc(patientId)
                                           .stream().limit(5).toList();

    // 3. Descargar contenido de reportes desde S3
    List<String> reportContents = reports.stream()
        .map(r -> s3Service.downloadTextContent(r.getS3Key()))
        .toList();

    // 4. Construir prompt para OpenAI
    String prompt = buildClinicalHistoryPrompt(patient, visits, reportContents);

    // 5. Generar resumen con OpenAI
    String summary = openAIService.generateClinicalHistorySummary(prompt);

    // 6. Subir resumen a S3
    String s3Key = generateClinicalHistoryS3Key(patientId);
    String s3Url = s3Service.uploadTextContent(summary, s3Key);

    // 7. Guardar registro en ClinicalHistoryRecord
    ClinicalHistoryRecord record = ClinicalHistoryRecord.builder()
        .patient(patient)
        .s3Url(s3Url)
        .createdAt(LocalDateTime.now())
        .build();

    clinicalHistoryRecordRepository.save(record);

    return record;
}

@Override
public ClinicalHistoryRecord getLatestRecord(UUID patientId) {
    return clinicalHistoryRecordRepository.findTopByPatientIdOrderByCreatedAtDesc(patientId);
} 

private String buildClinicalHistoryPrompt(Patient patient, List<MedicalVisit> visits, List<String> reportContents) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("You are a board-certified physician. Generate a comprehensive clinical summary for the following patient.\n\n");
    prompt.append("PATIENT INFORMATION:\n");
    prompt.append("- Name: ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("\n");
    prompt.append("- Gender: ").append(patient.getGender()).append("\n");
    prompt.append("- Birth Date: ").append(patient.getBirthDate()).append("\n");
    prompt.append("- CURP: ").append(patient.getCurp()).append("\n\n");

    prompt.append("MEDICAL VISIT HISTORY:\n");
    for (MedicalVisit visit : visits) {
        prompt.append("• Date: ").append(visit.getVisitDate()).append("\n");
        prompt.append("  Diagnosis: ").append(visit.getDiagnosis()).append("\n");
        prompt.append("  Recommendations: ").append(visit.getRecommendations()).append("\n");
        prompt.append("  Notes: ").append(visit.getNotes()).append("\n\n");
    }

    prompt.append("RECENT STUDY REPORTS:\n");
    for (String content : reportContents) {
        prompt.append(content).append("\n\n");
    }

    prompt.append("Please summarize the patient's clinical history, highlight important diagnoses, risk factors, and relevant findings from recent studies. Provide recommendations for future care and follow-up.\n");

    return prompt.toString();
}

private String generateClinicalHistoryS3Key(UUID patientId) {
    long timestamp = System.currentTimeMillis();
    return String.format("clinical-history/%d_%s_summary.txt", timestamp, patientId.toString());
}
}
