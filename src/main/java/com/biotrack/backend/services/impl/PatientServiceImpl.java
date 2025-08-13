package com.biotrack.backend.services.impl;

import com.biotrack.backend.dto.PrimaryHospitalDTO;
import com.biotrack.backend.models.BloodSample;
import com.biotrack.backend.models.ClinicalHistoryRecord;
import com.biotrack.backend.models.DnaSample;
import com.biotrack.backend.models.Hospital;
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
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
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

    // 3. Construir prompt para OpenAI - REPORTE TÉCNICO
    String technicalPrompt = buildClinicalHistoryPrompt(patient, visits, reports);

    // 4. Generar resumen técnico con OpenAI
    String technicalSummary = openAIService.generateClinicalHistorySummary(technicalPrompt);

    // 5. Construir prompt para OpenAI - REPORTE PATIENT-FRIENDLY
    String patientFriendlyPrompt = buildPatientFriendlyClinicalPrompt(patient, visits, reports, technicalSummary);

    // 6. Generar resumen patient-friendly con OpenAI
    String patientFriendlySummary = openAIService.generateClinicalHistorySummary(patientFriendlyPrompt);

    // 7. Subir resumen técnico a S3
    String technicalS3Key = generateClinicalHistoryS3Key(patientId, false);
    String technicalS3Url = s3Service.uploadTextContent(technicalSummary, technicalS3Key);

    // 8. Subir resumen patient-friendly a S3
    String patientFriendlyS3Key = generateClinicalHistoryS3Key(patientId, true);
    String patientFriendlyS3Url = s3Service.uploadTextContent(patientFriendlySummary, patientFriendlyS3Key);

    // 9. Guardar registro en ClinicalHistoryRecord con ambas URLs
    ClinicalHistoryRecord record = ClinicalHistoryRecord.builder()
        .patient(patient)
        .s3Url(technicalS3Url)           // URL del reporte técnico
        .s3UrlPatient(patientFriendlyS3Url)  // URL del reporte patient-friendly
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
    prompt.append("        \"idMuestra\": \"123e4567-e89b-12d3-a456-426614174000\",\n");
    prompt.append("        \"modeloAnalizador\": \"Sysmex XN-1000\",\n");
    prompt.append("        \"hallazgosPrincipales\": \"El paciente presentó niveles elevados de colesterol total...\"\n");
    prompt.append("      }\n");
    prompt.append("      // ... más estudios ...\n");
    prompt.append("    ],\n");
    prompt.append("    \"resumen\": {\n");
    prompt.append("      \"texto\": \"Jane Smith, una mujer de 35 años...\",\n");
    prompt.append("      \"enfermedadesDetectadas\": [\"EHNA\", \"Colesterol elevado\"],\n");
    prompt.append("      \"evidenciaRespalda\": [\n");
    prompt.append("        {\n");
    prompt.append("          \"enfermedad\": \"Colesterol elevado\",\n");
    prompt.append("          \"idMuestraRespaldo\": \"123e4567-e89b-12d3-a456-426614174000\",\n");
    prompt.append("          \"hallazgoEspecifico\": \"Colesterol total: 195 mg/dL (elevado)\"\n");
    prompt.append("        }\n");
    prompt.append("      ]\n");
    prompt.append("    },\n");
    prompt.append("    \"recomendaciones\": [\n");
    prompt.append("      \"Debe continuar con la dieta...\",\n");
    prompt.append("      \"Monitoreo regular de enzimas hepáticas...\"\n");
    prompt.append("    ],\n");
    prompt.append("    \"correlacionesClinitas\": {\n");
    prompt.append("      \"analisisProgresion\": \"Análisis de cómo han evolucionado los síntomas y hallazgos a lo largo del tiempo\",\n");
    prompt.append("      \"patronesIdentificados\": [\n");
    prompt.append("        {\n");
    prompt.append("          \"patron\": \"Descripción del patrón clínico identificado\",\n");
    prompt.append("          \"evidenciaRespaldo\": [\n");
    prompt.append("            {\n");
    prompt.append("              \"idMuestra\": \"UUID de la muestra que respalda este patrón\",\n");
    prompt.append("              \"hallazgo\": \"Hallazgo específico de laboratorio o clínico\"\n");
    prompt.append("            }\n");
    prompt.append("          ],\n");
    prompt.append("          \"fechasRelevantes\": [\"Fechas de visitas médicas que muestran este patrón\"]\n");
    prompt.append("        }\n");
    prompt.append("      ],\n");
    prompt.append("      \"hallazgosNoExplicados\": [\n");
    prompt.append("        {\n");
    prompt.append("          \"hallazgo\": \"Hallazgo que requiere investigación adicional\",\n");
    prompt.append("          \"idMuestra\": \"UUID de la muestra que muestra este hallazgo\",\n");
    prompt.append("          \"recomendacionInvestigacion\": \"Qué estudios adicionales se recomiendan\"\n");
    prompt.append("        }\n");
    prompt.append("      ]\n");
    prompt.append("    },\n");
    prompt.append("    \"trazabilidadEvidencia\": {\n");
    prompt.append("      \"muestrasAnalizadas\": [\"Lista completa de IDs de muestras incluidas en este resumen\"],\n");
    prompt.append("      \"visitasMedicasReferenciadas\": [\"Lista de fechas de visitas médicas analizadas\"],\n");
    prompt.append("      \"nivelConfianzaResumen\": \"Alto/Medio/Bajo - basado en la cantidad y calidad de evidencia disponible\"\n");
    prompt.append("    }\n");
    prompt.append("  }\n");
    prompt.append("}\n\n");

    prompt.append("CRITICAL ANALYSIS GUIDELINES:\n");
    prompt.append("• MANDATORY: Every clinical finding, disease detection, or medical correlation MUST be backed by specific sample IDs (idMuestra)\n");
    prompt.append("• TRACEABILITY: All medical conclusions must reference the specific samples that support them\n");
    prompt.append("• Use only the sample IDs provided in the medical reports and studies\n");
    prompt.append("• When identifying patterns or progressions, reference specific visit dates and sample IDs\n");
    prompt.append("• Clearly separate confirmed findings (with sample evidence) from clinical observations\n");
    prompt.append("• If a clinical finding cannot be supported by laboratory evidence, clearly state this in hallazgosNoExplicados\n");
    prompt.append("• Maintain medical accuracy and avoid speculation not supported by data\n");
    prompt.append("• Provide actionable recommendations based on evidence-backed findings\n\n");

    prompt.append("EVIDENCE REQUIREMENTS:\n");
    prompt.append("• Every disease in 'enfermedadesDetectadas' must have corresponding evidence in 'evidenciaRespalda'\n");
    prompt.append("• Every clinical pattern must reference specific sample IDs and visit dates\n");
    prompt.append("• Use only the sample IDs provided in the study reports\n");
    prompt.append("• Maintain scientific rigor and evidence-based conclusions\n");
    prompt.append("• Include all sample IDs in 'trazabilidadEvidencia.muestrasAnalizadas'\n\n");

    prompt.append("Now, using the real patient data below, generate the summary in the EXACT JSON structure above. ");
    prompt.append("REMEMBER: Always include specific sample IDs (idMuestra) when making clinical correlations to maintain full traceability.\n\n");

    // Información real del paciente
    prompt.append("PATIENT INFORMATION:\n");
    prompt.append("- Name: ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("\n");
    prompt.append("- Birth Date: ").append(patient.getBirthDate()).append("\n");
    prompt.append("- CURP: ").append(patient.getCurp()).append("\n\n");

    prompt.append("MEDICAL VISIT HISTORY:\n");
    for (MedicalVisit visit : visits) {
        prompt.append("- Visit ID: ").append(visit.getId()).append("\n");
        prompt.append("  Date: ").append(visit.getVisitDate()).append("\n");
        prompt.append("  Diagnosis: ").append(visit.getDiagnosis()).append("\n");
        prompt.append("  Recommendations: ").append(visit.getRecommendations()).append("\n");
        prompt.append("  Notes: ").append(visit.getNotes()).append("\n\n");
    }

    prompt.append("RECENT STUDY REPORTS (with Sample IDs for traceability):\n");
    for (Report report : reports) {
        var sample = report.getSample();
        prompt.append("- Sample ID: ").append(sample.getId()).append("\n");
        prompt.append("  Date: ").append(sample.getCollectionDate()).append("\n");
        prompt.append("  Type: ").append(sample.getType()).append("\n");
        
        if (sample instanceof BloodSample blood) {
            prompt.append("  Analyzer Model: ").append(blood.getAnalyzerModel()).append("\n");
        }
        if (sample instanceof DnaSample dna) {
            prompt.append("  Extraction Method: ").append(dna.getExtractionMethod()).append("\n");
        }
        if (sample instanceof SalivaSample saliva) {
            prompt.append("  Collection Method: ").append(saliva.getCollectionMethod()).append("\n");
        }
        
        String content = s3Service.downloadTextContent(report.getS3Key());
        prompt.append("  Main Findings: ").append(content).append("\n\n");
    }

    prompt.append("Generate the clinical summary using the EXACT JSON structure provided, ensuring all clinical findings are properly traced to their supporting sample IDs.\n");
    prompt.append("Finally and IMPORTANT, create all the response in Spanish, the only thing that keeps in English is Json variable names.\n");

    return prompt.toString();
}

private String buildPatientFriendlyClinicalPrompt(Patient patient, List<MedicalVisit> visits, List<Report> reports, String technicalSummary) {
    StringBuilder prompt = new StringBuilder();
    
    prompt.append("You are a compassionate medical communicator specializing in patient education. ");
    prompt.append("Your task is to create a patient-friendly clinical summary based on the technical medical summary provided. ");
    prompt.append("This summary should be easily understood by patients and their families, using simple language while maintaining medical accuracy.\n\n");
    
    prompt.append("IMPORTANT: Your response MUST be a valid JSON object with the following structure. DO NOT return plain text, markdown, or any other format. Only return the JSON object.\n\n");
    
    prompt.append("EXACT JSON STRUCTURE:\n");
    prompt.append("{\n");
    prompt.append("  \"resumen_clinico_paciente\": {\n");
    prompt.append("    \"informacion_paciente\": {\n");
    prompt.append("      \"nombre\": \"").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("\",\n");
    prompt.append("      \"fecha_nacimiento\": \"").append(patient.getBirthDate()).append("\",\n");
    prompt.append("      \"edad_aproximada\": \"Calcular edad basada en fecha de nacimiento\"\n");
    prompt.append("    },\n");
    prompt.append("    \"resumen_de_tu_salud\": {\n");
    prompt.append("      \"mensaje_principal\": \"Mensaje principal sobre el estado general de salud del paciente en términos simples\",\n");
    prompt.append("      \"que_se_analizo\": \"Explicación simple de qué estudios y análisis se realizaron\",\n");
    prompt.append("      \"periodo_analizado\": \"Descripción del tiempo que cubren estos estudios\",\n");
    prompt.append("      \"hallazgos_importantes\": \"Los hallazgos más relevantes explicados de manera comprensible\"\n");
    prompt.append("    },\n");
    prompt.append("    \"tu_historial_medico\": {\n");
    prompt.append("      \"visitas_recientes\": [\n");
    prompt.append("        {\n");
    prompt.append("          \"fecha\": \"YYYY-MM-DD\",\n");
    prompt.append("          \"motivo_consulta\": \"Por qué fuiste al doctor en términos simples\",\n");
    prompt.append("          \"que_encontraron\": \"Qué descubrió el doctor durante esa visita\",\n");
    prompt.append("          \"recomendaciones_principales\": \"Las recomendaciones más importantes que te dieron\"\n");
    prompt.append("        }\n");
    prompt.append("      ],\n");
    prompt.append("      \"progreso_de_tu_salud\": \"Cómo ha cambiado tu salud a lo largo del tiempo según las visitas\"\n");
    prompt.append("    },\n");
    prompt.append("    \"resultados_de_estudios\": {\n");
    prompt.append("      \"estudios_realizados\": [\n");
    prompt.append("        {\n");
    prompt.append("          \"fecha_estudio\": \"YYYY-MM-DD\",\n");
    prompt.append("          \"tipo_estudio\": \"Tipo de estudio en términos simples (ej: análisis de sangre, estudio genético)\",\n");
    prompt.append("          \"que_midieron\": \"Qué aspectos de tu salud se analizaron\",\n");
    prompt.append("          \"resultados_principales\": \"Los resultados más importantes explicados de forma comprensible\",\n");
    prompt.append("          \"que_significa_para_ti\": \"Qué significan estos resultados para tu salud\"\n");
    prompt.append("        }\n");
    prompt.append("      ],\n");
    prompt.append("      \"tendencias_importantes\": \"Patrones o cambios importantes que se observaron en tus estudios\"\n");
    prompt.append("    },\n");
    prompt.append("    \"condiciones_identificadas\": {\n");
    prompt.append("      \"condiciones_actuales\": [\n");
    prompt.append("        {\n");
    prompt.append("          \"nombre_condicion\": \"Nombre de la condición en términos comprensibles\",\n");
    prompt.append("          \"que_significa\": \"Explicación simple de qué es esta condición\",\n");
    prompt.append("          \"como_te_afecta\": \"Cómo puede afectar tu día a día\",\n");
    prompt.append("          \"evidencia_que_lo_respalda\": \"Qué estudios o síntomas apoyan este diagnóstico\",\n");
    prompt.append("          \"nivel_preocupacion\": \"Bajo/Moderado/Alto - qué tan preocupante es esto\"\n");
    prompt.append("        }\n");
    prompt.append("      ],\n");
    prompt.append("      \"areas_de_atencion\": \"Aspectos de tu salud que requieren seguimiento pero no son diagnósticos definitivos\"\n");
    prompt.append("    },\n");
    prompt.append("    \"plan_de_cuidados\": {\n");
    prompt.append("      \"acciones_inmediatas\": [\n");
    prompt.append("        {\n");
    prompt.append("          \"accion\": \"Qué necesitas hacer pronto\",\n");
    prompt.append("          \"por_que_es_importante\": \"Por qué es necesario hacer esto\",\n");
    prompt.append("          \"cuando_hacerlo\": \"Cuándo debes completar esta acción\"\n");
    prompt.append("        }\n");
    prompt.append("      ],\n");
    prompt.append("      \"cambios_estilo_vida\": [\n");
    prompt.append("        {\n");
    prompt.append("          \"recomendacion\": \"Cambio específico recomendado\",\n");
    prompt.append("          \"beneficio_esperado\": \"Cómo te ayudará este cambio\",\n");
    prompt.append("          \"facilidad_implementacion\": \"Fácil/Moderado/Desafiante\"\n");
    prompt.append("        }\n");
    prompt.append("      ],\n");
    prompt.append("      \"seguimiento_medico\": \"Con qué frecuencia debes ver a tu doctor y qué tipo de citas necesitas\"\n");
    prompt.append("    },\n");
    prompt.append("    \"preguntas_para_tu_doctor\": {\n");
    prompt.append("      \"preguntas_sugeridas\": [\n");
    prompt.append("        \"Pregunta importante que podrías hacerle a tu doctor\",\n");
    prompt.append("        \"Otra pregunta relevante sobre tu salud\"\n");
    prompt.append("      ],\n");
    prompt.append("      \"temas_a_discutir\": \"Temas importantes que deberías comentar en tu próxima cita\"\n");
    prompt.append("    },\n");
    prompt.append("    \"apoyo_y_recursos\": {\n");
    prompt.append("      \"mensaje_de_apoyo\": \"Mensaje positivo y de apoyo para el paciente\",\n");
    prompt.append("      \"proximos_pasos\": \"Los siguientes pasos más importantes en tu cuidado médico\",\n");
    prompt.append("      \"cuando_buscar_ayuda\": \"Señales de alarma o síntomas que requieren atención médica inmediata\"\n");
    prompt.append("    },\n");
    prompt.append("    \"notas_importantes\": {\n");
    prompt.append("      \"limitaciones\": \"Qué no cubre este resumen y qué otras evaluaciones podrían ser necesarias\",\n");
    prompt.append("      \"actualizacion\": \"Cuándo se debería actualizar este resumen\",\n");
    prompt.append("      \"confidencialidad\": \"Recordatorio sobre la privacidad de la información médica\"\n");
    prompt.append("    }\n");
    prompt.append("  }\n");
    prompt.append("}\n\n");
    
    prompt.append("COMMUNICATION GUIDELINES:\n");
    prompt.append("• Use simple, everyday language that a person without medical training can understand\n");
    prompt.append("• Avoid medical jargon; when medical terms are necessary, explain them clearly\n");
    prompt.append("• Be compassionate and supportive in tone\n");
    prompt.append("• Focus on actionable information the patient can use\n");
    prompt.append("• Be honest about findings while being encouraging when appropriate\n");
    prompt.append("• Emphasize the importance of working with their healthcare team\n");
    prompt.append("• Use positive framing when possible without minimizing real concerns\n");
    prompt.append("• Make complex medical relationships understandable through analogies or simple explanations\n\n");
    
    prompt.append("PATIENT CONTEXT:\n");
    prompt.append("This summary is for ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append(", ");
    prompt.append("born on ").append(patient.getBirthDate()).append(".\n\n");
    
    prompt.append("TECHNICAL MEDICAL SUMMARY (to be translated into patient-friendly language):\n");
    prompt.append(technicalSummary).append("\n\n");
    
    prompt.append("MEDICAL VISITS CONTEXT:\n");
    for (MedicalVisit visit : visits) {
        prompt.append("Visit Date: ").append(visit.getVisitDate()).append("\n");
        prompt.append("Diagnosis: ").append(visit.getDiagnosis()).append("\n");
        prompt.append("Recommendations: ").append(visit.getRecommendations()).append("\n");
        prompt.append("Notes: ").append(visit.getNotes()).append("\n\n");
    }
    
    prompt.append("STUDY REPORTS CONTEXT:\n");
    for (Report report : reports) {
        var sample = report.getSample();
        prompt.append("Study Date: ").append(sample.getCollectionDate()).append("\n");
        prompt.append("Sample Type: ").append(sample.getType()).append("\n");
        
        String content = s3Service.downloadTextContent(report.getS3Key());
        prompt.append("Study Findings: ").append(content).append("\n\n");
    }
    
    prompt.append("Generate the patient-friendly clinical summary using the EXACT JSON structure provided above. ");
    prompt.append("Transform the technical information into language that empowers the patient to understand ");
    prompt.append("and participate actively in their healthcare journey.\n");
    prompt.append("Finally and IMPORTANT, create all the response in Spanish, the only thing that keeps in English is Json variable names.\n");

    return prompt.toString();
}

private String generateClinicalHistoryS3Key(UUID patientId, boolean isPatientFriendly) {
    long timestamp = System.currentTimeMillis();
    String suffix = isPatientFriendly ? "patient_friendly_summary" : "technical_summary";
    return String.format("clinical-history/%d_%s_%s.json", timestamp, patientId.toString(), suffix);
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

@Override
public String getLatestSummaryTextPatientFriendly(UUID patientId) {
    ClinicalHistoryRecord record = getLatestRecord(patientId);
    if (record == null || record.getS3UrlPatient() == null) {
        throw new RuntimeException("No summary file found for this patient");
    }
    // Extrae la key de S3 desde la URL
    String s3Url = record.getS3UrlPatient();
    String bucketPattern = ".s3.amazonaws.com/";
    int keyStartIndex = s3Url.indexOf(bucketPattern);
    if (keyStartIndex == -1) {
        throw new IllegalArgumentException("Invalid S3 URL format: " + s3Url);
    }
    String s3Key = s3Url.substring(keyStartIndex + bucketPattern.length());
    return s3Service.downloadTextContent(s3Key);
}

@Override
public Optional<PrimaryHospitalDTO> getPrimaryHospital(UUID patientId) {
        Optional<Patient> patientOpt = patientRepository.findById(patientId);
        
        if (patientOpt.isEmpty()) {
            return Optional.empty();
        }
        
        Patient patient = patientOpt.get();
        
        // Verificar si el paciente tiene hospitales asignados
        if (patient.getHospitals() == null || patient.getHospitals().isEmpty()) {
            return Optional.empty();
        }
        
        // Obtener el primer hospital (índice 0)
        Hospital primaryHospital = patient.getHospitals().get(0);
        
        return Optional.of(PrimaryHospitalDTO.fromHospital(primaryHospital));
    }
}
