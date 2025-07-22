package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.BloodSample;
import com.biotrack.backend.models.ClinicalHistoryRecord;
import com.biotrack.backend.models.DnaSample;
import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.Report;
import com.biotrack.backend.models.SalivaSample;
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
    String prompt = buildClinicalHistoryPrompt(patient, visits, reports);

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

private String buildClinicalHistoryPrompt(Patient patient, List<MedicalVisit> visits, List<Report> reports) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("You are a board-certified physician. Your task is to generate a comprehensive clinical summary for the following patient. \n")
          .append("You MUST strictly follow the structure shown in the EXAMPLE at the end of this prompt. For each medical visit and each study report, ALWAYS display the date and a clear summary as shown. Do not omit any visit or report. Do not invent data. Use the real data provided.\n\n");

    prompt.append("PATIENT INFORMATION:\n");
    prompt.append("- Name: ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("\n");
    prompt.append("- Gender: ").append(patient.getGender()).append("\n");
    prompt.append("- Birth Date: ").append(patient.getBirthDate()).append("\n");
    prompt.append("- CURP: ").append(patient.getCurp()).append("\n\n");

    prompt.append("MEDICAL VISIT HISTORY:\n");
    prompt.append("For each visit, display the date and a concise summary including diagnosis, recommendations, and relevant notes.\n");
    for (MedicalVisit visit : visits) {
        prompt.append("Visit Date: ").append(visit.getVisitDate()).append("\n");
        prompt.append("Summary: ");
        prompt.append("Diagnosis: ").append(visit.getDiagnosis()).append(". ");
        prompt.append("Recommendations: ").append(visit.getRecommendations()).append(". ");
        prompt.append("Notes: ").append(visit.getNotes()).append("\n\n");
    }

    prompt.append("RECENT STUDY REPORTS:\n");
    prompt.append("For each study, display the collection date, sample type, and a summary of the main findings. If available, include analyzer model (for blood), extraction method (for DNA), or collection method (for saliva).\n");
    for (Report report : reports) {
        var sample = report.getSample();
        prompt.append("Study Date: ").append(sample.getCollectionDate()).append("\n");
        prompt.append("Sample Type: ").append(sample.getType()).append("\n");
        if (sample instanceof BloodSample blood) {
            prompt.append("Analyzer Model: ").append(blood.getAnalyzerModel()).append("\n");
        }
        if (sample instanceof DnaSample dna) {
            prompt.append("Extraction Method: ").append(dna.getExtractionMethod()).append("\n");
        }
        if (sample instanceof SalivaSample saliva) {
            prompt.append("Collection Method: ").append(saliva.getCollectionMethod()).append("\n");
        }
        prompt.append("Main Findings: ");
        String content = s3Service.downloadTextContent(report.getS3Key());
        prompt.append(content).append("\n\n");
    }

    prompt.append("INSTRUCTIONS FOR THE SUMMARY:\n");
    prompt.append("- For each medical visit, explicitly state the date and summarize the diagnosis, recommendations, and notes.\n");
    prompt.append("- For each study report, explicitly state the collection date, sample type,include analyzer model (for blood), extraction method (for DNA), or collection method (for saliva) and summarize the main findings.\n");
    prompt.append("- Highlight important diagnoses, risk factors, and relevant findings from recent studies.\n");
    prompt.append("- Provide clear recommendations for future care and follow-up.\n");

    // === EXAMPLE STRUCTURE ===
    prompt.append("IMPORTANT: The following is ONLY AN EXAMPLE OF THE STRUCTURE you must follow. DO NOT invent or copy this data. ALWAYS use the real patient data provided above.\n\n");
    prompt.append("PATIENT SUMMARY:\n\n");
    prompt.append("Patient Name: Name\n");
    prompt.append("Date of Birth: Birthdate\n");
    prompt.append("CURP: Curp\n\n");
    prompt.append("MEDICAL VISIT HISTORY:\n\n");
    prompt.append("1. Visit Date: 2025-07-01(Example)\n");
    prompt.append("   - Diagnosis: Diagnosis resume\n");
    prompt.append("   - Recommendations: Recommendation resume\n");
    prompt.append("   - Notes: Notes resume\n\n");
    prompt.append("2. Visit Date: 2025-07-10\n");
    prompt.append("   - Diagnosis: \n");
    prompt.append("   - Recommendations: \n");
    prompt.append("   - Notes: \n\n");
    prompt.append("3. Visit Date: 2025-07-18\n");
    prompt.append("   - Diagnosis:\n");
    prompt.append("   - Recommendations: \n");
    prompt.append("   - Notes: \n\n");
    prompt.append("//More medical visits can be shown depending on the amount of visits that were passed on prompt\n\n");
    prompt.append("RECENT STUDY REPORTS:\n\n");
    prompt.append("1. Study Date: 2025-07-19 (Example)\n");
    prompt.append("   - Sample Type: Blood (Example)\n");
    prompt.append("   - Analyzer Model: Sysmex XN-1000 (Example)\n");
    prompt.append("   - Main Findings: The patient presented with abnormal levels of glucose, total cholesterol, LDL cholesterol, and triglycerides, suggesting …(Example)\n\n");
    prompt.append("2. Study Date: \n");
    prompt.append("   - Sample Type: \n");
    prompt.append("   - Analyzer Model: \n");
    prompt.append("   - Main Findings: \n\n");
    prompt.append("3. Study Date: \n");
    prompt.append("   - Sample Type: DNA (Example)\n");
    prompt.append("   - Extraction Method: Silica Column (Example)\n");
    prompt.append("   - Main Findings:\n\n");
    prompt.append("4. Study Date: \n");
    prompt.append("   - Sample Type: Blood (Example)\n");
    prompt.append("   - Analyzer Model: \n");
    prompt.append("   - Main Findings: \n\n");
    prompt.append("5. Study Date:\n");
    prompt.append("   - Sample Type: Saliva (Example)\n");
    prompt.append("   - Collection Method : Passive drool (Example)\n");
    prompt.append("   - Main Findings: Normal volume and viscosity of saliva sample. No specific … (Example)\n\n");
    prompt.append("SUMMARY:\n");
    prompt.append("Juan Perez, a 45-year-old male, has a history of hypertension and was recently diagnosed with …(Example)\n\n");
    prompt.append("RECOMMENDATIONS:\n");
    prompt.append("Juan should continue with the prescribed oral iron supplement and … (Example)\n");

    return prompt.toString();
}

private String generateClinicalHistoryS3Key(UUID patientId) {
    long timestamp = System.currentTimeMillis();
    return String.format("clinical-history/%d_%s_summary.txt", timestamp, patientId.toString());
}
}
