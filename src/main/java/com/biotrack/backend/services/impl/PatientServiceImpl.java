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
        patient.setCreatedAt(LocalDateTime.now().toLocalDate());
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
        Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Patient not found"));

        // Elimina la relación con hospitales
        patient.getHospitals().forEach(hospital -> hospital.getActivePatients().remove(patient));
        patient.getHospitals().clear();

        patientRepository.save(patient); // Actualiza relaciones

        patientRepository.deleteById(id); // Ahora sí elimina el paciente
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
    prompt.append("You are a board-certified physician. Your task is to generate a comprehensive clinical summary for the following patient.\n");
    prompt.append("IMPORTANT: Your response MUST be a valid JSON object with the following structure and field names. DO NOT return plain text, markdown, or any other format. Only return the JSON object.\n\n");

    prompt.append("EXAMPLE JSON STRUCTURE (use real patient data, do not invent or copy this example):\n");
    prompt.append("{\n");
    prompt.append("  \"reporteMedico\": {\n");
    prompt.append("    \"paciente\": {\n");
    prompt.append("      \"nombre\": \"Jane Smith\",\n");
    prompt.append("      \"fechaNacimiento\": \"1990-05-15\",\n");
    prompt.append("      \"curp\": \"SMIJ900515HDFRN9\"\n");
    prompt.append("    },\n");
    prompt.append("    \"historialMedico\": [\n");
    prompt.append("      {\n");
    prompt.append("        \"fechaVisita\": \"2025-06-05\",\n");
    prompt.append("        \"diagnostico\": \"Observación inicial. Posible fatiga inducida por estrés.\",\n");
    prompt.append("        \"recomendaciones\": [\"Monitorear la calidad del sueño\", \"Reducir la ingesta de cafeína\", \"Revisar exámenes generales si los síntomas persisten\"],\n");
    prompt.append("        \"notas\": \"Primera visita del paciente. Refiere fatiga leve por las tardes. Sin otros síntomas relevantes.\"\n");
    prompt.append("      }\n");
    prompt.append("      // ... más visitas ...\n");
    prompt.append("    ],\n");
    prompt.append("    \"reportesEstudiosRecientes\": [\n");
    prompt.append("      {\n");
    prompt.append("        \"fechaEstudio\": \"2025-07-19\",\n");
    prompt.append("        \"tipoMuestra\": \"Sangre\",\n");
    prompt.append("        \"modeloAnalizador\": \"Sysmex XN-1000\",\n");
    prompt.append("        \"hallazgosPrincipales\": \"El paciente presentó niveles elevados de colesterol total...\"\n");
    prompt.append("      }\n");
    prompt.append("      // ... más estudios ...\n");
    prompt.append("    ],\n");
    prompt.append("    \"resumen\": {\n");
    prompt.append("      \"texto\": \"Jane Smith, una mujer de 35 años...\",\n");
    prompt.append("      \"enfermedadesDetectadas\": [\"EHNA\", \"Colesterol elevado\"]\n");
    prompt.append("    },\n");
    prompt.append("    \"recomendaciones\": [\n");
    prompt.append("      \"Debe continuar con la dieta...\",\n");
    prompt.append("      \"Monitoreo regular de enzimas hepáticas...\"\n");
    prompt.append("    ]\n");
    prompt.append("  }\n");
    prompt.append("}\n\n");

    prompt.append("Now, using the real patient data below, generate the summary in the EXACT JSON structure above. Do not invent or omit any data. Use the patient's real visits, studies, and information.\n\n");

    // Aquí puedes agregar la información real del paciente, visitas y reportes como contexto para la IA
    prompt.append("PATIENT INFORMATION:\n");
    prompt.append("- Name: ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("\n");
    prompt.append("- Birth Date: ").append(patient.getBirthDate()).append("\n");
    prompt.append("- CURP: ").append(patient.getCurp()).append("\n\n");

    prompt.append("MEDICAL VISIT HISTORY:\n");
    for (MedicalVisit visit : visits) {
        prompt.append("- Date: ").append(visit.getVisitDate()).append(", Diagnosis: ").append(visit.getDiagnosis())
              .append(", Recommendations: ").append(visit.getRecommendations())
              .append(", Notes: ").append(visit.getNotes()).append("\n");
    }

    prompt.append("\nRECENT STUDY REPORTS:\n");
    for (Report report : reports) {
        var sample = report.getSample();
        prompt.append("- Date: ").append(sample.getCollectionDate())
              .append(", Type: ").append(sample.getType());
        if (sample instanceof BloodSample blood) {
            prompt.append(", Analyzer Model: ").append(blood.getAnalyzerModel());
        }
        if (sample instanceof DnaSample dna) {
            prompt.append(", Extraction Method: ").append(dna.getExtractionMethod());
        }
        if (sample instanceof SalivaSample saliva) {
            prompt.append(", Collection Method: ").append(saliva.getCollectionMethod());
        }
        String content = s3Service.downloadTextContent(report.getS3Key());
        prompt.append(", Main Findings: ").append(content).append("\n");
    }

    return prompt.toString();
}

private String generateClinicalHistoryS3Key(UUID patientId) {
    long timestamp = System.currentTimeMillis();
    return String.format("clinical-history/%d_%s_summary.txt", timestamp, patientId.toString());
}

@Override
public List<Patient> searchPatients(String firstName, String lastName) {
    // Si ambos son nulos o vacíos, retorna todos
    if ((firstName == null || firstName.isBlank()) && (lastName == null || lastName.isBlank())) {
        return patientRepository.findAll();
    }
    // Si solo uno es nulo, usa "" para que el filtro sea inclusivo
    return patientRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(
        firstName != null ? firstName : "",
        lastName != null ? lastName : ""
    );
}

@Override
public String getLatestSummaryText(UUID patientId) {
    ClinicalHistoryRecord record = getLatestRecord(patientId);
    if (record == null || record.getS3Url() == null) {
        throw new RuntimeException("No summary file found for this patient");
    }
    // Extrae la key de S3 desde la URL
    String s3Url = record.getS3Url();
    String bucketPattern = ".s3.amazonaws.com/";
    int keyStartIndex = s3Url.indexOf(bucketPattern);
    if (keyStartIndex == -1) {
        throw new IllegalArgumentException("Invalid S3 URL format: " + s3Url);
    }
    String s3Key = s3Url.substring(keyStartIndex + bucketPattern.length());
    return s3Service.downloadTextContent(s3Key);
}
}
