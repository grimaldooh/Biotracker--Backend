package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.Mutation;
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
}