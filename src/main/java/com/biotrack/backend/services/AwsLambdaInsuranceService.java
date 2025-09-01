package com.biotrack.backend.services;

import com.biotrack.backend.dto.insurance.LambdaInsuranceRequestDTO;
import com.biotrack.backend.dto.insurance.LambdaInsuranceResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AwsLambdaInsuranceService {

    private final LambdaClient lambdaClient;
    private final ObjectMapper objectMapper;
    private final String functionName;

    public AwsLambdaInsuranceService(LambdaClient lambdaClient,
                                   ObjectMapper objectMapper,
                                   @Value("${aws.lambda.function.insurance-calculator}") String functionName) {
        this.lambdaClient = lambdaClient;
        this.objectMapper = objectMapper;
        this.functionName = functionName;
    }

    public LambdaInsuranceResponseDTO calculateInsurancePremium(LambdaInsuranceRequestDTO request) {
        try {
            log.info("Calling AWS Lambda function: {} for patient calculation", functionName);
            
            // Convertir request a JSON
            String jsonPayload = objectMapper.writeValueAsString(request);
            log.debug("Lambda payload: {}", jsonPayload);

            // Crear request para Lambda
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(SdkBytes.fromUtf8String(jsonPayload))
                    .build();

            // Ejecutar Lambda
            InvokeResponse response = lambdaClient.invoke(invokeRequest);
            
            // Verificar que la ejecución fue exitosa
            if (response.statusCode() != 200) {
                throw new RuntimeException("Lambda function failed with status: " + response.statusCode());
            }

            // Parsear respuesta
            String responsePayload = response.payload().asUtf8String();
            log.debug("Lambda response: {}", responsePayload);
            
            LambdaInsuranceResponseDTO result = objectMapper.readValue(responsePayload, LambdaInsuranceResponseDTO.class);
            
            log.info("Insurance calculation completed successfully. Premium: ${}", result.getMonthlyPremium());
            return result;

        } catch (Exception e) {
            log.error("Error calling AWS Lambda function for insurance calculation", e);
            throw new RuntimeException("Failed to calculate insurance premium via Lambda", e);
        }
    }
}