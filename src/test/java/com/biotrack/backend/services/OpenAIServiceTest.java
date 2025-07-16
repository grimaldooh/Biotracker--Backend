package com.biotrack.backend.services;

import com.biotrack.backend.models.Mutation;
import com.biotrack.backend.models.enums.Relevance;
import com.biotrack.backend.services.impl.OpenAIServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAIServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private OpenAIService openAIService;

    @BeforeEach
    void setUp() {
        openAIService = new OpenAIServiceImpl(restTemplate);
        
        // Configurar propiedades usando ReflectionTestUtils
        ReflectionTestUtils.setField(openAIService, "apiUrl", "https://api.openai.com/v1/chat/completions");
        ReflectionTestUtils.setField(openAIService, "model", "gpt-4o-mini");
        ReflectionTestUtils.setField(openAIService, "maxTokens", 2048);
        ReflectionTestUtils.setField(openAIService, "temperature", 0.3);
        ReflectionTestUtils.setField(openAIService, "apiKey", "sk-test-key");
    }

    @Test
    void generateGeneticReport_ShouldReturnReport_WhenOpenAIRespondsCorrectly() {
        // Arrange
        List<Mutation> mutations = createTestMutations();
        String patientInfo = "Patient: Juan Pérez, Male, Born: 1990-05-10";
        
        String mockResponse = """
                {
                    "choices": [
                        {
                            "message": {
                                "content": "# GENETIC ANALYSIS REPORT\\n\\n## EXECUTIVE SUMMARY\\nThe genetic analysis reveals significant findings..."
                            }
                        }
                    ]
                }
                """;
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok(mockResponse));

        // Act
        String result = openAIService.generateGeneticReport(mutations, patientInfo);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("GENETIC ANALYSIS REPORT"));
        assertTrue(result.contains("EXECUTIVE SUMMARY"));
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
    }

    @Test
    void generateGeneticReport_ShouldThrowException_WhenTooManyMutations() {
        // Arrange
        List<Mutation> tooManyMutations = createLargeMutationList(60); // Más de 50
        String patientInfo = "Test patient";

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> openAIService.generateGeneticReport(tooManyMutations, patientInfo));
        
        assertTrue(exception.getMessage().contains("Too many mutations"));
    }

    @Test
    void generateGeneticReport_ShouldThrowException_WhenOpenAIReturnsError() {
        // Arrange
        List<Mutation> mutations = createTestMutations();
        String patientInfo = "Test patient";
        
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenThrow(new RuntimeException("OpenAI API Error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> openAIService.generateGeneticReport(mutations, patientInfo));
        
        assertTrue(exception.getMessage().contains("Error generating genetic report"));
    }

    @Test
    void isConfigured_ShouldReturnTrue_WhenApiKeyIsValid() {
        // Act
        boolean result = openAIService.isConfigured();

        // Assert
        assertTrue(result);
    }

    @Test
    void isConfigured_ShouldReturnFalse_WhenApiKeyIsEmpty() {
        // Arrange
        ReflectionTestUtils.setField(openAIService, "apiKey", "");

        // Act
        boolean result = openAIService.isConfigured();

        // Assert
        assertFalse(result);
    }

    @Test
    void isConfigured_ShouldReturnFalse_WhenApiKeyIsPlaceholder() {
        // Arrange
        ReflectionTestUtils.setField(openAIService, "apiKey", "${OPENAI_API_KEY}");

        // Act
        boolean result = openAIService.isConfigured();

        // Assert
        assertFalse(result);
    }

    @Test
    void getModelUsed_ShouldReturnConfiguredModel() {
        // Act
        String result = openAIService.getModelUsed();

        // Assert
        assertEquals("gpt-4o-mini", result);
    }

    private List<Mutation> createTestMutations() {
        return Arrays.asList(
                Mutation.builder()
                        .id(UUID.randomUUID())
                        .gene("BRCA1")
                        .chromosome("17")
                        .type("SNV")
                        .relevance(Relevance.HIGH)
                        .comment("Pathogenic variant associated with breast cancer")
                        .build(),
                Mutation.builder()
                        .id(UUID.randomUUID())
                        .gene("TP53")
                        .chromosome("17")
                        .type("DELETION")
                        .relevance(Relevance.MEDIUM)
                        .comment("Tumor suppressor gene variant")
                        .build()
        );
    }

    private List<Mutation> createLargeMutationList(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> Mutation.builder()
                        .id(UUID.randomUUID())
                        .gene("GENE" + i)
                        .chromosome("CHR" + (i % 22 + 1))
                        .type("SNV")
                        .relevance(Relevance.LOW)
                        .comment("Test mutation " + i)
                        .build())
                .toList();
    }
}