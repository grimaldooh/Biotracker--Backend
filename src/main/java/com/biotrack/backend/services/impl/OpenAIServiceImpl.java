package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.Patient;
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

    @Value("${openai.max-tokens:2048}")
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
     * Construye el prompt especializado para análisis genético - versión avanzada
     */
    private String buildGeneticPrompt(List<Mutation> mutations, String patientInfo) {
        // Validar que no tengamos demasiadas mutaciones (límite de tokens)
        if (mutations.size() > 50) {
            throw new RuntimeException("Too many mutations for single report generation. Maximum: 50");
        }
        
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("You are a board-certified clinical geneticist with expertise in molecular genetics and personalized medicine. ");
        prompt.append("Analyze the following genetic variants and provide a comprehensive clinical interpretation.\n\n");
        
        prompt.append("PATIENT CONTEXT:\n");
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
        prompt.append("Please structure your analysis as follows:\n\n");
        
        prompt.append("1. EXECUTIVE SUMMARY\n");
        prompt.append("   • Overall genetic risk assessment\n");
        prompt.append("   • Key findings summary\n\n");
        
        prompt.append("2. VARIANT CLASSIFICATION & ANALYSIS\n");
        prompt.append("   • Individual variant interpretation\n");
        prompt.append("   • Pathogenicity assessment (following ACMG guidelines when applicable)\n");
        prompt.append("   • Population frequency considerations\n\n");
        
        prompt.append("3. CLINICAL SIGNIFICANCE\n");
        prompt.append("   • Disease associations\n");
        prompt.append("   • Phenotypic implications\n");
        prompt.append("   • Inheritance patterns\n\n");
        
        prompt.append("4. CLINICAL RECOMMENDATIONS\n");
        prompt.append("   • Monitoring recommendations\n");
        prompt.append("   • Therapeutic considerations\n");
        prompt.append("   • Family screening suggestions\n");
        prompt.append("   • Lifestyle modifications if applicable\n\n");
        
        prompt.append("5. LIMITATIONS & CONSIDERATIONS\n");
        prompt.append("   • Technical limitations\n");
        prompt.append("   • Variant interpretation limitations\n");
        prompt.append("   • Recommendations for additional testing\n\n");
        
        prompt.append("FORMAT GUIDELINES:\n");
        prompt.append("• Use clear, professional medical language\n");
        prompt.append("• Include relevant references to established genetic databases (ClinVar, OMIM) when applicable\n");
        prompt.append("• Maintain objectivity and evidence-based interpretations\n");
        prompt.append("• Clearly distinguish between established facts and clinical recommendations");
        
        // Verificar longitud aproximada (4 caracteres ≈ 1 token)
        if (prompt.length() > 8000) { // ~2000 tokens máximo para el prompt
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
        prompt.append("Your response MUST be a valid JSON object with the following structure and field names. Use simple, patient-friendly language that avoids medical jargon. Do NOT return plain text, markdown, or any other format. Only return the JSON object.\n\n");

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
        prompt.append("    }\n");
        prompt.append("  }\n");
        prompt.append("}\n\n");

        prompt.append("WRITING GUIDELINES:\n");
        prompt.append("• Use language a high school graduate can understand\n");
        prompt.append("• Avoid medical jargon - if you must use a medical term, explain it immediately\n");
        prompt.append("• Be encouraging and supportive in tone\n");
        prompt.append("• Focus on actionable information the patient can use\n");
        prompt.append("• Be honest but not alarming - balance accuracy with reassurance where appropriate\n");
        prompt.append("• Use 'you' and 'your' to personalize the information\n\n");

        prompt.append("Now, using the real patient and sample data provided above, generate the patient-friendly report in the EXACT JSON structure. Make it educational, supportive, and easy to understand.\n");

        // Check prompt length
        if (prompt.length() > 8000) {
            throw new RuntimeException("Prompt too long. Consider reducing patient/sample info.");
        }

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
}