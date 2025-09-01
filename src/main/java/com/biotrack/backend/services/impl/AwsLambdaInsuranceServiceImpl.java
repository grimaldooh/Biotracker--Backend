package com.biotrack.backend.services.impl;

import com.biotrack.backend.dto.insurance.LambdaInsuranceRequestDTO;
import com.biotrack.backend.dto.insurance.LambdaInsuranceResponseDTO;
import com.biotrack.backend.services.AwsLambdaInsuranceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsLambdaInsuranceServiceImpl implements AwsLambdaInsuranceService {
    
    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;
    
    @Value("${aws.lambda.function.insurance-calculator:insurance_lambda}")
    private String functionName;
    
    @Override
    public LambdaInsuranceResponseDTO calculateInsurancePremium(LambdaInsuranceRequestDTO request) {
        log.info("🚀 Iniciando invocación de Lambda: {}", functionName);
        log.debug("📤 Request payload: {}", request);
        
        try {
            // 1. Validar request
            validateRequest(request);
            
            // 2. Convertir a JSON
            String jsonPayload = objectMapper.writeValueAsString(request);
            log.debug("📝 JSON Payload: {}", jsonPayload);
            
            // 3. Preparar invocación
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(SdkBytes.fromUtf8String(jsonPayload))
                    .build();
            
            // 4. Invocar Lambda
            long startTime = System.currentTimeMillis();
            InvokeResponse response = lambdaClient.invoke(invokeRequest);
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("⏱️ Lambda invocación completada en {}ms", duration);
            log.debug("📊 Status Code: {}", response.statusCode());
            
            // 5. Procesar respuesta
            String responsePayload = response.payload().asUtf8String();
            log.debug("📥 Response payload: {}", responsePayload);
            
            // 6. Verificar errores de Lambda
            if (response.statusCode() != 200) {
                throw new RuntimeException("Lambda function returned error. Status: " + response.statusCode());
            }
            
            // 7. Deserializar respuesta
            LambdaInsuranceResponseDTO result = objectMapper.readValue(responsePayload, LambdaInsuranceResponseDTO.class);
            
            // 8. Verificar errores en el payload
            if (Boolean.TRUE.equals(result.getError())) {
                throw new RuntimeException("Lambda calculation error: " + result.getMessage());
            }
            
            log.info("✅ Prima calculada exitosamente: ${}", result.getMonthlyPremium());
            log.info("📈 Risk Score: {}", result.getRiskScore());
            
            return result;
            
        } catch (LambdaException e) {
            log.error("❌ AWS Lambda error", e);
            throw new RuntimeException("Failed to invoke Lambda function: " + e.getMessage(), e);
            
        } catch (JsonProcessingException e) {
            log.error("❌ JSON processing error", e);
            throw new RuntimeException("Failed to process JSON payload", e);
            
        } catch (Exception e) {
            log.error("❌ Unexpected error calling Lambda", e);
            throw new RuntimeException("Failed to calculate insurance premium", e);
        }
    }
    
    @Override
    public boolean testLambdaConnection() {
        log.info("🧪 Testing Lambda connection...");
        
        try {
            LambdaInsuranceRequestDTO testRequest = LambdaInsuranceRequestDTO.builder()
                    .age(30)
                    .coverageAmount(100000.0)
                    .coverageType("BASIC")
                    .build();
            
            LambdaInsuranceResponseDTO response = calculateInsurancePremium(testRequest);
            
            boolean isValid = response.getMonthlyPremium() != null && 
                             response.getMonthlyPremium() > 0 && 
                             response.getRiskScore() != null;
            
            log.info("🎯 Lambda connection test: {}", isValid ? "PASSED" : "FAILED");
            return isValid;
            
        } catch (Exception e) {
            log.error("❌ Lambda connection test failed", e);
            return false;
        }
    }
    
    private void validateRequest(LambdaInsuranceRequestDTO request) {
        log.debug("🔍 Validando request de Lambda...");
        
        if (request.getAge() == null) {
            throw new IllegalArgumentException("Age is required");
        }
        
        if (request.getCoverageAmount() == null) {
            throw new IllegalArgumentException("Coverage amount is required");
        }
        
        // Validaciones de rangos
        if (request.getAge() < 18 || request.getAge() > 100) {
            throw new IllegalArgumentException("Age must be between 18 and 100");
        }
        
        if (request.getCoverageAmount() < 10000 || request.getCoverageAmount() > 2000000) {
            throw new IllegalArgumentException("Coverage amount must be between $10,000 and $2,000,000");
        }
        
        // Validar coverage type si está presente
        if (request.getCoverageType() != null) {
            String coverageType = request.getCoverageType().toUpperCase();
            if (!coverageType.matches("BASIC|STANDARD|PREMIUM|COMPREHENSIVE")) {
                throw new IllegalArgumentException("Invalid coverage type: " + request.getCoverageType());
            }
        }
        
        // Validar rangos opcionales
        if (request.getLifestyleScore() != null && (request.getLifestyleScore() < 1 || request.getLifestyleScore() > 10)) {
            throw new IllegalArgumentException("Lifestyle score must be between 1 and 10");
        }
        
        if (request.getExerciseFrequency() != null && (request.getExerciseFrequency() < 0 || request.getExerciseFrequency() > 7)) {
            throw new IllegalArgumentException("Exercise frequency must be between 0 and 7");
        }
        
        log.debug("✅ Request validation passed");
    }
}