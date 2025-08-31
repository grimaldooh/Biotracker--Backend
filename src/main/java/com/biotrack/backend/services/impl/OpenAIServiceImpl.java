package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.dto.MedicationAnalysisDTO;
import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.services.OpenAIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIServiceImpl implements OpenAIService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.url}")
    private String apiUrl;

    @Value("${openai.model:gpt-4}")
    private String model;

    @Value("${openai.max-tokens:4096}")
    private int maxTokens;

    @Value("${openai.temperature:0.3}")
    private double temperature;

    @Value("${openai.api.key}")
    private String apiKey;

    public OpenAIServiceImpl(@Qualifier("openAIRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generateGeneticReport(List<Mutation> mutations, String patientInfo) {
        if (!isConfigured()) {
            throw new RuntimeException("OpenAI service is not properly configured");
        }

        try {
            String prompt = buildGeneticPrompt(mutations, patientInfo);
            Map<String, Object> requestBody = buildRequestBody(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return extractResponseContent(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Error generating genetic report with OpenAI: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && 
               !apiKey.startsWith("${") && // No es variable sin resolver
               apiUrl != null && !apiUrl.trim().isEmpty();
    }

    @Override
    public String getModelUsed() {
        return model;
    }

    /**
     * Construye el prompt especializado para análisis genético - versión JSON estructurada con correlación médica trazable
     */
    private String buildGeneticPrompt(List<Mutation> mutations, String patientInfo) {
        // Validar que no tengamos demasiadas mutaciones (límite de tokens)
        if (mutations.size() > 50) {
            throw new RuntimeException("Too many mutations for single report generation. Maximum: 50");
        }
        
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a board-certified clinical geneticist with expertise in molecular genetics and personalized medicine. ");
        prompt.append("Analyze the following genetic variants and provide a comprehensive clinical interpretation in English, ");
        prompt.append("correlating the genetic findings with the patient's medical history and recent laboratory studies.\n\n");
        
        prompt.append("PATIENT CONTEXT AND MEDICAL HISTORY:\n");
        prompt.append(patientInfo != null ? patientInfo : "Patient information not provided");
        prompt.append("\n\n");
        
        prompt.append("GENETIC VARIANTS IDENTIFIED:\n");
        for (int i = 0; i < mutations.size(); i++) {
            Mutation mutation = mutations.get(i);
            prompt.append(String.format("Variant %d:\n", i + 1));
            prompt.append(String.format("  • Gene: %s\n", mutation.getGene()));
            prompt.append(String.format("  • Chromosome: %s\n", mutation.getChromosome()));
            prompt.append(String.format("  • Mutation Type: %s\n", mutation.getType()));
            prompt.append(String.format("  • Clinical Relevance: %s\n", mutation.getRelevance()));
            prompt.append(String.format("  • Additional Notes: %s\n\n", mutation.getComment()));
        }
        
        prompt.append("REPORT REQUIREMENTS:\n");
        prompt.append("Your response MUST be a valid JSON object with the following structure and field names. Do NOT return plain text, markdown, or any other format. Only return the JSON object.\n\n");
        
        prompt.append("EXACT JSON STRUCTURE (use real patient and variant data, do not invent or copy this example):\n");
        prompt.append("{\n");
        prompt.append("  \"genetic_analysis_report\": {\n");
        prompt.append("    \"patient_summary\": {\n");
        prompt.append("      \"name\": \"Patient's Name\",\n");
        prompt.append("      \"analysis_date\": \"YYYY-MM-DD\",\n");
        prompt.append("      \"total_variants_analyzed\": 0\n");
        prompt.append("    },\n");
        prompt.append("    \"executive_summary\": {\n");
        prompt.append("      \"overall_risk_assessment\": \"High/Medium/Low risk assessment with brief explanation\",\n");
        prompt.append("      \"key_findings\": \"Most significant genetic findings in 2-3 sentences\",\n");
        prompt.append("      \"clinical_priority\": \"Immediate/Routine/Monitoring - level of clinical attention needed\"\n");
        prompt.append("    },\n");
        prompt.append("    \"variant_analysis\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"gene\": \"Gene name\",\n");
        prompt.append("        \"chromosome\": \"Chromosome location\",\n");
        prompt.append("        \"variant_type\": \"Type of genetic variant\",\n");
        prompt.append("        \"pathogenicity_classification\": \"Pathogenic/Likely Pathogenic/VUS/Likely Benign/Benign\",\n");
        prompt.append("        \"clinical_significance\": \"Detailed explanation of what this variant means clinically\",\n");
        prompt.append("        \"population_frequency\": \"How common this variant is in the general population\",\n");
        prompt.append("        \"inheritance_pattern\": \"Autosomal dominant/recessive/X-linked/etc.\",\n");
        prompt.append("        \"associated_conditions\": [\"List of diseases or conditions associated with this variant\"]\n");
        prompt.append("      }\n");
        prompt.append("    ],\n");
        prompt.append("    \"medical_history_correlation\": {\n");
        prompt.append("      \"genetic_explanation_for_symptoms\": {\n");
        prompt.append("        \"analysis\": \"How the identified genetic variants may explain the patient's documented medical history and symptoms\",\n");
        prompt.append("        \"referenced_sample_evidence\": [\n");
        prompt.append("          {\n");
        prompt.append("            \"sample_id\": \"UUID of the referenced sample\",\n");
        prompt.append("            \"finding\": \"What clinical or laboratory finding this sample supports (e.g., 'HIGH cholesterol', 'Elevated ALT/AST', 'Normal platelets')\"\n");
        prompt.append("          }\n");
        prompt.append("          // ...more referenced samples as needed...\n");
        prompt.append("        ]\n");
        prompt.append("      },\n");
        prompt.append("      \"laboratory_findings_correlation\": {\n");
        prompt.append("        \"analysis\": \"Correlation between genetic variants and recent laboratory abnormalities (elevated enzymes, cholesterol, etc.)\",\n");
        prompt.append("        \"referenced_sample_ids\": [\"List of sample IDs that show these abnormalities\"]\n");
        prompt.append("      },\n");
        prompt.append("      \"progression_pattern_analysis\": {\n");
        prompt.append("        \"analysis\": \"How the patient's medical timeline aligns with expected genetic disease progression\",\n");
        prompt.append("        \"referenced_medical_visits\": [\"Relevant medical visit dates that support this progression pattern\"]\n");
        prompt.append("      },\n");
        prompt.append("      \"unexplained_findings\": {\n");
        prompt.append("        \"analysis\": \"Medical findings that are NOT explained by the identified genetic variants\",\n");
        prompt.append("        \"referenced_sample_ids\": [\"List of sample IDs showing unexplained findings\"],\n");
        prompt.append("        \"additional_testing_needed\": \"Recommendations for additional tests to explain these findings\"\n");
        prompt.append("      },\n");
        prompt.append("      \"genetic_predisposition_confirmation\": {\n");
        prompt.append("        \"analysis\": \"Which aspects of the patient's health history support or contradict the genetic findings\",\n");
        prompt.append("        \"supporting_evidence\": [\"List of clinical findings that support genetic interpretations with their sample IDs\"],\n");
        prompt.append("        \"contradicting_evidence\": [\"List of clinical findings that contradict genetic interpretations with their sample IDs\"]\n");
        prompt.append("      },\n");
        prompt.append("      \"family_history_implications\": \"What the genetic findings suggest about potential family member risks based on inheritance patterns\"\n");
        prompt.append("    },\n");
        prompt.append("    \"clinical_implications\": {\n");
        prompt.append("      \"disease_risk\": \"Assessment of disease risk based on identified variants and current medical status\",\n");
        prompt.append("      \"phenotypic_manifestations\": \"Potential physical or clinical signs to watch for, considering current symptoms\",\n");
        prompt.append("      \"penetrance_information\": \"Likelihood that genetic variants will actually cause disease, given current medical presentation\",\n");
        prompt.append("      \"age_of_onset_considerations\": \"When symptoms might appear or progress, considering patient's current age and medical timeline\"\n");
        prompt.append("    },\n");
        prompt.append("    \"clinical_recommendations\": {\n");
        prompt.append("      \"immediate_actions\": [\"List of urgent medical actions needed based on genetic and clinical correlation\"],\n");
        prompt.append("      \"monitoring_schedule\": \"Recommended frequency and type of medical monitoring tailored to genetic risk and current health status\",\n");
        prompt.append("      \"therapeutic_considerations\": \"Potential treatments or interventions considering both genetic predisposition and current medical conditions\",\n");
        prompt.append("      \"lifestyle_modifications\": [\"Diet, exercise, environmental factors specifically relevant to genetic findings and current health issues\"],\n");
        prompt.append("      \"family_screening\": \"Recommendations for testing family members based on identified variants and inheritance patterns\",\n");
        prompt.append("      \"genetic_counseling\": \"Whether genetic counseling is recommended and why, considering family implications\"\n");
        prompt.append("    },\n");
        prompt.append("    \"technical_details\": {\n");
        prompt.append("      \"methodology\": \"Brief description of genetic testing method used\",\n");
        prompt.append("      \"coverage_limitations\": \"What areas of the genome were not fully analyzed\",\n");
        prompt.append("      \"variant_interpretation_databases\": [\"ClinVar\", \"OMIM\", \"Other databases referenced\"],\n");
        prompt.append("      \"analysis_limitations\": \"Technical or interpretive limitations of this analysis\"\n");
        prompt.append("    },\n");
        prompt.append("    \"follow_up_recommendations\": {\n");
        prompt.append("      \"additional_testing\": \"Recommendations for further genetic or clinical testing based on correlation analysis\",\n");
        prompt.append("      \"specialist_referrals\": [\"Types of medical specialists to consult considering genetic findings and medical history\"],\n");
        prompt.append("      \"reanalysis_timeline\": \"When genetic data should be reanalyzed with updated databases\"\n");
        prompt.append("    },\n");
        prompt.append("    \"important_disclaimers\": {\n");
        prompt.append("      \"interpretation_certainty\": \"Level of confidence in the interpretation given available medical history\",\n");
        prompt.append("      \"evolving_knowledge\": \"Note that genetic knowledge continues to evolve\",\n");
        prompt.append("      \"clinical_correlation\": \"Importance of correlating with clinical presentation and ongoing medical care\",\n");
        prompt.append("      \"evidence_limitations\": \"Any limitations in the evidence used for correlations and interpretations\"\n");
        prompt.append("    }\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");
        
        prompt.append("CRITICAL ANALYSIS GUIDELINES:\n");
        prompt.append("• Follow ACMG/AMP guidelines for variant classification when applicable\n");
        prompt.append("• Reference established genetic databases (ClinVar, OMIM, gnomAD) appropriately\n");
        prompt.append("• Use evidence-based interpretations and avoid speculation\n");
        prompt.append("• MANDATORY FOR MEDICAL CORRELATIONS: Always include sample IDs when referencing laboratory findings or medical history\n");
        prompt.append("• TRACEABILITY: Every medical history correlation MUST reference specific sample IDs from the patient's medical reports\n");
        prompt.append("• Use only the sample IDs provided in the patient medical history context (idMuestra fields)\n");
        prompt.append("• If a clinical finding cannot be explained by the provided variants, clearly state this\n");
        prompt.append("• Distinguish between confirmed genetic findings and clinical inferences\n");
        prompt.append("• Explain which medical findings support or contradict the genetic interpretations with specific sample ID references\n");
        prompt.append("• Consider disease progression timelines and how they align with genetic expectations\n");
        prompt.append("• Identify gaps where genetics cannot explain observed medical findings\n");
        prompt.append("• Provide actionable clinical guidance that integrates genetic risk with current health status\n");
        prompt.append("• Consider family implications based on inheritance patterns and medical history\n\n");
        
        prompt.append("MEDICAL CORRELATION EVIDENCE REQUIREMENTS:\n");
        prompt.append("• Every laboratory finding correlation must be backed by specific sample IDs (idMuestra)\n");
        prompt.append("• Use only the sample IDs provided in the reportesEstudiosRecientes section\n");
        prompt.append("• When referencing medical visits, use the fechaVisita dates provided in historialMedico\n");
        prompt.append("• Maintain scientific rigor and avoid overinterpretation\n");
        prompt.append("• Clearly separate genetic analysis from medical history correlation\n\n");
        
        prompt.append("Now, using the real patient data, medical history, and genetic variant information provided above, generate the genetic analysis report in the EXACT JSON structure. ");
        prompt.append("Pay special attention to correlating the genetic findings with the patient's documented medical timeline and laboratory abnormalities. ");
        prompt.append("REMEMBER: Always include specific sample IDs (idMuestra) when referencing laboratory findings to maintain full traceability of medical correlations.\n");
        prompt.append("Finally and IMPORTANT, create all the response in Spanish, never change json variable names.\n");

        // Verificar longitud aproximada (4 caracteres ≈ 1 token)
        if (prompt.length() > 30000) { // Ajustado para la nueva estructura
            throw new RuntimeException("Prompt too long. Consider reducing mutation count or patient info.");
        }
        
        return prompt.toString();
    }

    @Override
    public String generateClinicalReport(String patientInfo) {
        if (!isConfigured()) {
            throw new RuntimeException("OpenAI service is not properly configured");
        }

        try {
            String prompt = buildClinicalPrompt(patientInfo);
            Map<String, Object> requestBody = buildRequestBody(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return extractResponseContent(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Error generating clinical report with OpenAI: " + e.getMessage(), e);
        }
    }

    /**
     * Construye el prompt clínico para cualquier tipo de muestra (sangre, saliva, ADN).
     */
    private String buildClinicalPrompt(String patientInfo) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a board-certified clinical laboratory specialist. ");
        prompt.append("Analyze the following patient and sample information and generate a comprehensive clinical report in English.\n\n");

        prompt.append("PATIENT & SAMPLE CONTEXT:\n");
        prompt.append(patientInfo != null ? patientInfo : "Patient and sample information not provided");
        prompt.append("\n\n");

        prompt.append("REPORT REQUIREMENTS:\n");
        prompt.append("Your response MUST be a valid JSON object with the following structure and field names. Do NOT return plain text, markdown, or any other format. Only return the JSON object.\n\n");

        prompt.append("EXACT JSON STRUCTURE (use real patient and sample data, do not invent or copy this example):\n");
        prompt.append("{\n");
        prompt.append("  \"medical_study_report\": {\n");
        prompt.append("    \"patient_data\": {\n");
        prompt.append("      \"name\": \"Patient's Name\"\n");
        prompt.append("    },\n");
        prompt.append("    \"sample_information\": {\n");
        prompt.append("      \"sample_type\": \"Type of sample (e.g., 'Blood', 'Saliva', 'DNA')\",\n");
        prompt.append("      \"analyzer_model\": \"Analyzer model (e.g., 'Sysmex XN-1000')\",\n");
        prompt.append("      \"collection_date\": \"YYYY-MM-DD\",\n");
        prompt.append("      \"lab_notes\": \"Relevant lab notes\"\n");
        prompt.append("    },\n");
        prompt.append("    \"clinical_findings\": {\n");
        prompt.append("      \"executive_summary\": \"Concise summary of the most important findings.\",\n");
        prompt.append("      \"sample_analysis\": \"Detailed analysis of the sample results, with values and their meanings.\",\n");
        prompt.append("      \"clinical_significance\": \"Explanation of the impact of the findings on the patient's health.\"\n");
        prompt.append("    },\n");
        prompt.append("    \"recommendations\": {\n");
        prompt.append("      \"action_plan\": \"Recommendations on diet, exercise, medication, etc.\",\n");
        prompt.append("      \"analysis_limitations\": \"Possible limitations of the analysis (e.g., lack of patient history, genetic markers, etc.).\"\n");
        prompt.append("    }\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");

        prompt.append("Now, using the real patient and sample data provided above, generate the report in the EXACT JSON structure. Do not invent or omit any data. Use clear, professional medical language and maintain objectivity and evidence-based interpretations.\n");

        prompt.append("CRITICAL OUTPUT INSTRUCTIONS:\n");
        prompt.append("• Return ONLY the JSON object - no markdown, no backticks, no code blocks\n");
        prompt.append("• Do NOT wrap your response in ```json ``` or any other formatting\n");
        prompt.append("• Start your response directly with { and end with }\n");
        prompt.append("• Your entire response should be valid JSON that can be parsed directly\n\n");


        // Check prompt length (optional)
        if (prompt.length() > 8000) {
            throw new RuntimeException("Prompt too long. Consider reducing patient/sample info.");
        }

        return prompt.toString();
    }

    /**
     * Construye el prompt clínico orientado al paciente - lenguaje accesible y educativo
     */
    private String buildPatientFriendlyClinicalPrompt(String patientInfo) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a patient education specialist and clinical communicator. ");
        prompt.append("Your role is to translate complex medical information into clear, understandable language for patients and their families.\n\n");

        prompt.append("PATIENT & SAMPLE CONTEXT:\n");
        prompt.append(patientInfo != null ? patientInfo : "Patient and sample information not provided");
        prompt.append("\n\n");

        prompt.append("REPORT REQUIREMENTS:\n");
        prompt.append("Your response MUST be ONLY a valid JSON object. Do NOT include markdown formatting, backticks, or any other text. ");
        prompt.append("Return ONLY the JSON object without any wrapping or additional formatting.\n\n");

        prompt.append("EXACT JSON STRUCTURE (use real patient and sample data, write in simple terms):\n");
        prompt.append("{\n");
        prompt.append("  \"patient_friendly_report\": {\n");
        prompt.append("    \"your_test_summary\": {\n");
        prompt.append("      \"what_was_tested\": \"Simple explanation of what type of sample was analyzed (e.g., 'Your blood sample', 'Your saliva sample')\",\n");
        prompt.append("      \"when_tested\": \"YYYY-MM-DD\",\n");
        prompt.append("      \"main_findings\": \"One or two sentences explaining the most important results in simple terms\"\n");
        prompt.append("    },\n");
        prompt.append("    \"what_your_results_mean\": {\n");
        prompt.append("      \"in_simple_terms\": \"Explanation of results using everyday language, avoiding medical terminology\",\n");
        prompt.append("      \"what_is_normal\": \"Context about normal ranges or expected values\",\n");
        prompt.append("      \"your_specific_results\": \"How your results compare to normal ranges\"\n");
        prompt.append("    },\n");
        prompt.append("    \"health_impact\": {\n");
        prompt.append("      \"what_this_means_for_you\": \"Practical explanation of how these results might affect your health\",\n");
        prompt.append("      \"should_you_be_concerned\": \"Clear guidance on whether results indicate concern and why\",\n");
        prompt.append("      \"positive_aspects\": \"Any reassuring or positive findings to highlight\"\n");
        prompt.append("    },\n");
        prompt.append("    \"next_steps\": {\n");
        prompt.append("      \"immediate_actions\": \"What you should do right away, if anything\",\n");
        prompt.append("      \"lifestyle_tips\": \"Practical advice on diet, exercise, habits that could help\",\n");
        prompt.append("      \"follow_up_care\": \"When and why you might need additional tests or doctor visits\"\n");
        prompt.append("    },\n");
        prompt.append("    \"questions_to_ask\": {\n");
        prompt.append("      \"for_your_doctor\": [\"Suggested questions to discuss with your healthcare provider\"],\n");
        prompt.append("      \"understanding_better\": \"Areas where you might want more explanation\"\n");
        prompt.append("    },\n");
        prompt.append("    \"important_notes\": {\n");
        prompt.append("      \"limitations\": \"Simple explanation of what this test can and cannot tell you\",\n");
        prompt.append("      \"remember\": \"Key points to keep in mind about your results\"\n");
        prompt.append("    },\n");
        prompt.append("    \"ai_recommendation\": {\n");
        prompt.append("      \"specialist_needed\": \"true/false - whether a specialist consultation is recommended\",\n");
        prompt.append("      \"specialist_type\": \"Type of specialist recommended (e.g., 'Cardiólogo', 'Endocrinólogo', 'Gastroenterólogo', 'Nefrólogo', 'Hematólogo') or null if no specialist needed\",\n");
        prompt.append("      \"reason\": \"Brief explanation of why this specialist is recommended based on the results, or null if no specialist needed\",\n");
        prompt.append("      \"urgency\": \"Low/Medium/High - how urgent the consultation is, or null if no specialist needed\"\n");
        prompt.append("    }\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");

        prompt.append("CRITICAL OUTPUT INSTRUCTIONS:\n");
        prompt.append("• Return ONLY the JSON object - no markdown, no backticks, no code blocks\n");
        prompt.append("• Do NOT wrap your response in ```json ``` or any other formatting\n");
        prompt.append("• Start your response directly with { and end with }\n");
        prompt.append("• Your entire response should be valid JSON that can be parsed directly\n\n");

        prompt.append("WRITING GUIDELINES:\n");
        prompt.append("• Use language a high school graduate can understand\n");
        prompt.append("• Avoid medical jargon - if you must use a medical term, explain it immediately\n");
        prompt.append("• Be encouraging and supportive in tone\n");
        prompt.append("• Focus on actionable information the patient can use\n");
        prompt.append("• Be honest but not alarming - balance accuracy with reassurance where appropriate\n");
        prompt.append("• Use 'you' and 'your' to personalize the information\n\n");
        
        prompt.append("SPECIALIST RECOMMENDATION GUIDELINES:\n");
        prompt.append("• Analyze the clinical results carefully to determine if specialist consultation is needed\n");
        prompt.append("• Only recommend a specialist if the results show clear abnormalities that warrant specialized care\n");
        prompt.append("• Common specialist recommendations based on findings:\n");
        prompt.append("  - Cardiólogo: for heart-related issues, high cholesterol, blood pressure problems\n");
        prompt.append("  - Endocrinólogo: for diabetes, thyroid issues, hormonal imbalances\n");
        prompt.append("  - Gastroenterólogo: for liver function abnormalities, digestive issues\n");
        prompt.append("  - Nefrólogo: for kidney function problems\n");
        prompt.append("  - Hematólogo: for blood disorders, anemia, clotting issues\n");
        prompt.append("• Set specialist_needed to false and other fields to null if no specialist consultation is warranted\n");
        prompt.append("• Be conservative - only recommend specialists when clearly indicated by abnormal results\n\n");

        prompt.append("Now, using the real patient and sample data provided above, generate the patient-friendly report as a direct JSON object. ");
        prompt.append("Remember: NO markdown formatting, NO backticks, just pure JSON.\n");
        prompt.append("Finally and IMPORTANT, create all the response in Spanish, never change json variable names.\n");

        // Check prompt length
        if (prompt.length() > 8000) {
            throw new RuntimeException("Prompt too long. Consider reducing patient/sample info.");
        }

        return prompt.toString();
    }

    @Override
    public String generateMedicationCompatibilityReport(List<MedicationAnalysisDTO> medications, String clinicalContext) {
        if (!isConfigured()) {
            throw new RuntimeException("OpenAI service is not properly configured");
        }

        try {
            String prompt = buildMedicationCompatibilityPrompt(medications, clinicalContext);
            
           
            //int medicationAnalysisTokens = 3500;
            
            Map<String, Object> requestBody = buildRequestBody(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return extractResponseContent(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Error generating medication compatibility report with OpenAI: " + e.getMessage(), e);
        }
    }

    // ✅ NUEVO: Método para construir prompt de compatibilidad de medicamentos
    private String buildMedicationCompatibilityPrompt(List<MedicationAnalysisDTO> medications, String clinicalContext) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a clinical pharmacologist and medication safety expert. ");
        prompt.append("Analyze the following medication regimen for potential drug interactions, contraindications, ");
        prompt.append("and safety concerns considering the patient's clinical context.\n\n");
        
        // Contexto clínico del paciente
        prompt.append("PATIENT CLINICAL CONTEXT:\n");
        if (clinicalContext != null && !clinicalContext.trim().isEmpty()) {
            prompt.append(clinicalContext);
        } else {
            prompt.append("No clinical history available - base analysis solely on medication interactions.");
        }
        prompt.append("\n\n");
        
        // Lista de medicamentos
        prompt.append("CURRENT MEDICATION REGIMEN:\n");
        for (int i = 0; i < medications.size(); i++) {
            MedicationAnalysisDTO med = medications.get(i);
            prompt.append(String.format("Medication %d:\n", i + 1));
            prompt.append(String.format("  - Name: %s\n", med.name()));
            prompt.append(String.format("  - Brand: %s\n", med.brand() != null ? med.brand() : "Generic"));
            prompt.append(String.format("  - Active Substance: %s\n", med.activeSubstance() != null ? med.activeSubstance() : "Not specified"));
            prompt.append(String.format("  - Indication: %s\n", med.indication() != null ? med.indication() : "Not specified"));
            prompt.append(String.format("  - Dosage: %s\n", med.dosage()));
            prompt.append(String.format("  - Frequency: %s\n", med.frequency()));
            prompt.append(String.format("  - Start Date: %s\n", med.startDate()));
            prompt.append(String.format("  - End Date: %s\n", med.endDate() != null ? med.endDate() : "Ongoing"));
            prompt.append(String.format("  - Prescribed By: %s\n\n", med.prescribedBy() != null ? med.prescribedBy() : "Not specified"));
        }
        
        prompt.append("ANALYSIS REQUIREMENTS:\n");
        prompt.append("Provide a comprehensive medication safety analysis in the following JSON structure.\n");
        prompt.append("Your response MUST be a valid JSON object. Do NOT return plain text, markdown, or any other format.\n\n");
        
        prompt.append("EXACT JSON STRUCTURE:\n");
        prompt.append("{\n");
        prompt.append("  \"medication_compatibility_report\": {\n");
        prompt.append("    \"analysis_summary\": {\n");
        prompt.append("      \"total_medications_analyzed\": ").append(medications.size()).append(",\n");
        prompt.append("      \"analysis_date\": \"YYYY-MM-DD\",\n");
        prompt.append("      \"overall_safety_assessment\": \"High Risk/Moderate Risk/Low Risk/Safe\",\n");
        prompt.append("      \"key_concerns\": \"Brief summary of most critical findings\"\n");
        prompt.append("    },\n");
        prompt.append("    \"drug_interactions\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"interaction_type\": \"Major/Moderate/Minor\",\n");
        prompt.append("        \"medications_involved\": [\"List of medication names involved\"],\n");
        prompt.append("        \"mechanism\": \"How the interaction occurs\",\n");
        prompt.append("        \"clinical_significance\": \"What this means for the patient\",\n");
        prompt.append("        \"severity_level\": \"Critical/Serious/Moderate/Minor\",\n");
        prompt.append("        \"recommendations\": \"Specific actions to take\"\n");
        prompt.append("      }\n");
        prompt.append("    ],\n");
        prompt.append("    \"contraindications\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"medication_name\": \"Name of problematic medication\",\n");
        prompt.append("        \"contraindication_reason\": \"Why this medication is problematic\",\n");
        prompt.append("        \"clinical_context\": \"Patient condition that creates the contraindication\",\n");
        prompt.append("        \"risk_level\": \"Absolute/Relative\",\n");
        prompt.append("        \"alternative_suggestions\": \"Safer medication alternatives if available\"\n");
        prompt.append("      }\n");
        prompt.append("    ],\n");
        prompt.append("    \"dosage_concerns\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"medication_name\": \"Name of medication\",\n");
        prompt.append("        \"concern_type\": \"Overdose Risk/Underdose Risk/Inappropriate Frequency\",\n");
        prompt.append("        \"current_dosage\": \"Current prescribed dosage\",\n");
        prompt.append("        \"recommended_dosage\": \"Suggested dosage adjustment\",\n");
        prompt.append("        \"rationale\": \"Why adjustment is needed\"\n");
        prompt.append("      }\n");
        prompt.append("    ],\n");
        prompt.append("    \"clinical_context_analysis\": {\n");
        prompt.append("      \"medication_appropriateness\": \"How well the medications align with patient conditions\",\n");
        prompt.append("      \"therapeutic_gaps\": \"Conditions that might need additional treatment\",\n");
        prompt.append("      \"polypharmacy_assessment\": \"Evaluation of medication burden and potential for simplification\"\n");
        prompt.append("    },\n");
        prompt.append("    \"monitoring_recommendations\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"parameter\": \"What to monitor (e.g., 'Liver function', 'Blood pressure')\",\n");
        prompt.append("        \"frequency\": \"How often to monitor\",\n");
        prompt.append("        \"rationale\": \"Why monitoring is needed\",\n");
        prompt.append("        \"target_values\": \"What values to aim for\"\n");
        prompt.append("      }\n");
        prompt.append("    ],\n");
        prompt.append("    \"immediate_actions\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"priority\": \"Urgent/High/Medium/Low\",\n");
        prompt.append("        \"action\": \"Specific action to take\",\n");
        prompt.append("        \"timeframe\": \"When to complete this action\",\n");
        prompt.append("        \"rationale\": \"Why this action is needed\"\n");
        prompt.append("      }\n");
        prompt.append("    ],\n");
        prompt.append("    \"safety_score\": {\n");
        prompt.append("      \"overall_score\": \"0-100 (100 being safest)\",\n");
        prompt.append("      \"interaction_risk_score\": \"0-100\",\n");
        prompt.append("      \"appropriateness_score\": \"0-100\",\n");
        prompt.append("      \"monitoring_compliance_score\": \"0-100\"\n");
        prompt.append("    },\n");
        prompt.append("    \"recommendations_summary\": {\n");
        prompt.append("      \"continue_medications\": [\"Medications that are safe to continue\"],\n");
        prompt.append("      \"modify_medications\": [\"Medications that need dosage or timing changes\"],\n");
        prompt.append("      \"discontinue_medications\": [\"Medications that should be stopped\"],\n");
        prompt.append("      \"add_medications\": [\"Suggested additions for therapeutic gaps\"],\n");
        prompt.append("      \"specialist_referral_needed\": \"Whether consultation with specialist is recommended\"\n");
        prompt.append("    },\n");
        prompt.append("    \"disclaimer\": \"This analysis is for educational purposes and should not replace professional medical consultation. All medication changes should be supervised by a healthcare provider.\"\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");
        
        prompt.append("ANALYSIS GUIDELINES:\n");
        prompt.append("• Use evidence-based pharmacological principles\n");
        prompt.append("• Consider drug-drug, drug-disease, and drug-food interactions\n");
        prompt.append("• Evaluate appropriateness for patient's clinical context\n");
        prompt.append("• Prioritize patient safety above all other considerations\n");
        prompt.append("• Provide specific, actionable recommendations\n");
        prompt.append("• Consider medication adherence and practical aspects\n");
        prompt.append("• Reference established drug interaction databases when applicable\n\n");
        
        prompt.append("Generate the complete medication compatibility analysis using the exact JSON structure above. ");
        prompt.append("Base your analysis on the provided medications and clinical context.\n");
        prompt.append("Finally and IMPORTANT, create all the response in Spanish, never change json variable names.\n");

        return prompt.toString();
    }

    /**
     * Construye el cuerpo de la petición para OpenAI
     */
    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("temperature", temperature);

        // Formato de mensajes para Chat Completions API
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        
        requestBody.put("messages", List.of(message));

        return requestBody;
    }

    /**
     * Extrae el contenido de la respuesta de OpenAI
     */
    private String extractResponseContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");
            
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null) {
                    JsonNode content = message.get("content");
                    if (content != null) {
                        return content.asText();
                    }
                }
            }
            
            throw new RuntimeException("Invalid response format from OpenAI");
            
        } catch (Exception e) {
            throw new RuntimeException("Error parsing OpenAI response: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateClinicalHistorySummary(String prompt) {
        if (!isConfigured()) {
            throw new RuntimeException("OpenAI service is not properly configured");
        }
        try {
            Map<String, Object> requestBody = buildRequestBody(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return extractResponseContent(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Error generating clinical history summary with OpenAI: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePatientFriendlyClinicalReport(String patientInfo) {
        if (!isConfigured()) {
            throw new RuntimeException("OpenAI service is not properly configured");
        }

        try {
            String prompt = buildPatientFriendlyClinicalPrompt(patientInfo);
            Map<String, Object> requestBody = buildRequestBody(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return extractResponseContent(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Error generating patient-friendly clinical report with OpenAI: " + e.getMessage(), e);
        }
    }

    @Override
    public String generatePatientFriendlyGeneticReport(List<Mutation> mutations, String clinicalContext, String technicalReport) {
        if (!isConfigured()) {
            throw new RuntimeException("OpenAI service is not properly configured");
        }

        try {
            String prompt = buildPatientFriendlyGeneticPrompt(mutations, clinicalContext, technicalReport);
            Map<String, Object> requestBody = buildRequestBody(prompt);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            return extractResponseContent(response.getBody());

        } catch (Exception e) {
            throw new RuntimeException("Error generating patient-friendly genetic report with OpenAI: " + e.getMessage(), e);
        }
    }

    // ✅ NUEVO: Método para construir prompt genético patient-friendly
    private String buildPatientFriendlyGeneticPrompt(List<Mutation> mutations, String clinicalContext, String technicalReport) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a genetic counselor and patient education specialist with expertise in translating complex genetic information ");
        prompt.append("into understandable language for patients and their families. Your role is to take technical genetic analysis ");
        prompt.append("and make it accessible, supportive, and actionable for patients.\n\n");
        
        prompt.append("IMPORTANT: Your response MUST be a valid JSON object with the following structure. DO NOT return plain text, markdown, or any other format. Only return the JSON object.\n\n");
        
        prompt.append("TECHNICAL GENETIC REPORT (for reference):\n");
        prompt.append(technicalReport).append("\n\n");
        
        prompt.append("PATIENT CLINICAL CONTEXT:\n");
        prompt.append(clinicalContext != null ? clinicalContext : "No clinical context available");
        prompt.append("\n\n");
        
        prompt.append("GENETIC VARIANTS IDENTIFIED:\n");
        for (int i = 0; i < mutations.size(); i++) {
            Mutation mutation = mutations.get(i);
            prompt.append(String.format("Variant %d:\n", i + 1));
            prompt.append(String.format("  • Gene: %s\n", mutation.getGene()));
            prompt.append(String.format("  • Chromosome: %s\n", mutation.getChromosome()));
            prompt.append(String.format("  • Type: %s\n", mutation.getType()));
            prompt.append(String.format("  • Clinical Relevance: %s\n", mutation.getRelevance()));
            prompt.append(String.format("  • Notes: %s\n\n", mutation.getComment()));
        }
        
        prompt.append("EXACT JSON STRUCTURE:\n");
        prompt.append("{\n");
        prompt.append("  \"your_genetic_report\": {\n");
        prompt.append("    \"understanding_your_test\": {\n");
        prompt.append("      \"what_we_analyzed\": \"Simple explanation of what genetic testing was done\",\n");
        prompt.append("      \"test_date\": \"YYYY-MM-DD\",\n");
        prompt.append("      \"total_variants_found\": ").append(mutations.size()).append(",\n");
        prompt.append("      \"main_message\": \"One or two sentences summarizing the most important findings in simple terms\"\n");
        prompt.append("    },\n");
        prompt.append("    \"your_genetic_findings\": {\n");
        prompt.append("      \"overall_picture\": \"What your genetic results mean for your health in everyday language\",\n");
        prompt.append("      \"risk_level\": \"Low/Moderate/High - overall genetic risk assessment\",\n");
        prompt.append("      \"what_this_means_for_you\": \"Practical explanation of how these genetic findings might affect your life\",\n");
        prompt.append("      \"comparison_to_others\": \"How your genetic profile compares to the general population\"\n");
        prompt.append("    },\n");
        prompt.append("    \"your_specific_variants\": [\n");
        prompt.append("      {\n");
        prompt.append("        \"gene_name\": \"Name of the gene in simple terms\",\n");
        prompt.append("        \"what_this_gene_does\": \"Simple explanation of what this gene is responsible for in your body\",\n");
        prompt.append("        \"your_variant\": \"Description of the change found in your DNA\",\n");
        prompt.append("        \"what_it_means\": \"How this variant might affect your health in understandable terms\",\n");
        prompt.append("        \"how_common_is_it\": \"How frequently this variant is found in people\",\n");
        prompt.append("        \"inheritance_info\": \"Simple explanation of how you got this variant (from parents, etc.)\",\n");
        prompt.append("        \"concern_level\": \"Low/Medium/High - how concerning this variant is\",\n");
        prompt.append("        \"action_needed\": \"What, if anything, you should do about this finding\"\n");
        prompt.append("      }\n");
        prompt.append("    ],\n");
        prompt.append("    \"health_implications\": {\n");
        prompt.append("      \"immediate_concerns\": \"Any health issues you should be aware of right now\",\n");
        prompt.append("      \"long_term_outlook\": \"What these findings might mean for your future health\",\n");
        prompt.append("      \"lifestyle_impact\": \"How these results might affect your daily life, if at all\",\n");
        prompt.append("      \"preventive_opportunities\": \"Things you can do to reduce any genetic risks\"\n");
        prompt.append("    },\n");
        prompt.append("    \"family_considerations\": {\n");
        prompt.append("      \"family_risk\": \"What these results might mean for your family members\",\n");
        prompt.append("      \"inheritance_pattern\": \"Simple explanation of how these genetic traits are passed down\",\n");
        prompt.append("      \"family_testing_recommendations\": \"Whether family members should consider genetic testing\",\n");
        prompt.append("      \"children_considerations\": \"What this means if you have or plan to have children\"\n");
        prompt.append("    },\n");
        prompt.append("    \"your_action_plan\": {\n");
        prompt.append("      \"immediate_steps\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"action\": \"Specific action you should take\",\n");
        prompt.append("          \"why_important\": \"Why this action matters for your health\",\n");
        prompt.append("          \"timeline\": \"When you should complete this\"\n");
        prompt.append("        }\n");
        prompt.append("      ],\n");
        prompt.append("      \"lifestyle_recommendations\": [\n");
        prompt.append("        {\n");
        prompt.append("          \"category\": \"Diet/Exercise/Environment/etc.\",\n");
        prompt.append("          \"recommendation\": \"Specific suggestion tailored to your genetic profile\",\n");
        prompt.append("          \"benefit\": \"How this will help given your genetic findings\"\n");
        prompt.append("        }\n");
        prompt.append("      ],\n");
        prompt.append("      \"medical_monitoring\": {\n");
        prompt.append("        \"tests_to_discuss\": [\"Medical tests you should ask your doctor about\"],\n");
        prompt.append("        \"frequency\": \"How often you should have check-ups related to these findings\",\n");
        prompt.append("        \"specialists_to_see\": [\"Types of doctors who might help with your genetic profile\"]\n");
        prompt.append("      }\n");
        prompt.append("    },\n");
        prompt.append("    \"understanding_genetics\": {\n");
        prompt.append("      \"genetics_101\": \"Simple explanation of how genetics work and why variants matter\",\n");
        prompt.append("      \"why_testing_matters\": \"Benefits of knowing your genetic information\",\n");
        prompt.append("      \"limitations_to_know\": \"What genetic testing can and cannot tell you\",\n");
        prompt.append("      \"future_discoveries\": \"How new genetic knowledge might affect your results over time\"\n");
        prompt.append("    },\n");
        prompt.append("    \"questions_and_support\": {\n");
        prompt.append("      \"questions_for_doctor\": [\n");
        prompt.append("        \"Important questions to ask your healthcare provider about these results\"\n");
        prompt.append("      ],\n");
        prompt.append("      \"questions_for_genetic_counselor\": [\n");
        prompt.append("        \"Questions specifically for a genetic counselor\"\n");
        prompt.append("      ],\n");
        prompt.append("      \"emotional_support\": \"Guidance on processing these genetic findings emotionally\",\n");
        prompt.append("      \"resources_to_explore\": [\"Helpful websites, support groups, or educational materials\"]\n");
        prompt.append("    },\n");
        prompt.append("    \"important_reminders\": {\n");
        prompt.append("      \"genetics_is_not_destiny\": \"Reassurance that genetic variants don't guarantee disease\",\n");
        prompt.append("      \"lifestyle_matters\": \"How your choices can influence genetic risk\",\n");
        prompt.append("      \"ongoing_relationship\": \"The importance of working with your healthcare team\",\n");
        prompt.append("      \"privacy_and_discrimination\": \"Information about genetic privacy and anti-discrimination laws\"\n");
        prompt.append("    },\n");
        prompt.append("    \"next_steps_summary\": {\n");
        prompt.append("      \"most_important_action\": \"The single most important thing to do next\",\n");
        prompt.append("      \"timeline_overview\": \"Overview of when to complete various recommended actions\",\n");
        prompt.append("      \"follow_up_plan\": \"When and why to revisit these genetic findings\"\n");
        prompt.append("    }\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");
        
        prompt.append("COMMUNICATION GUIDELINES:\n");
        prompt.append("• Use language that someone without scientific background can understand\n");
        prompt.append("• Avoid genetic jargon - when technical terms are necessary, explain them immediately\n");
        prompt.append("• Be supportive and reassuring while being honest about findings\n");
        prompt.append("• Emphasize that genetics is just one factor in health\n");
        prompt.append("• Focus on actionable information and empowerment\n");
        prompt.append("• Address common fears and misconceptions about genetic testing\n");
        prompt.append("• Encourage collaboration with healthcare providers\n");
        prompt.append("• Be culturally sensitive and inclusive in language\n");
        prompt.append("• Provide hope and emphasize the benefits of genetic knowledge\n\n");
        
        prompt.append("Using the technical genetic report and variant information provided above, create a comprehensive ");
        prompt.append("patient-friendly genetic report in the EXACT JSON structure. Make it educational, supportive, ");
        prompt.append("and empowering while maintaining scientific accuracy. Transform complex genetic concepts into ");
        prompt.append("language that helps the patient understand and act on their genetic information.\n");
        prompt.append("Finally and IMPORTANT, create all the response in Spanish, never change json variable names.\n");

        return prompt.toString();
    }
}