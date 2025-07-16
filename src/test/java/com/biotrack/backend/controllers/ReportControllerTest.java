package com.biotrack.backend.controllers;

import com.biotrack.backend.models.Report;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.models.enums.ReportStatus;
import com.biotrack.backend.services.OpenAIService;
import com.biotrack.backend.services.ReportService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @MockBean
    private OpenAIService openAIService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateReport_ShouldReturnCreated_WhenReportGeneratedSuccessfully() throws Exception {
        // Arrange
        UUID sampleId = UUID.fromString("38fa3d6b-d486-4337-bd2f-059cb1f10d6b");
        String patientInfo = "Additional patient information";
        
        Report mockReport = Report.builder()
                .id(UUID.randomUUID())
                .sample(Sample.builder().id(sampleId).build())
                .status(ReportStatus.COMPLETED)
                .s3Url("https://biotrack-results-files.s3.amazonaws.com/reports/test.txt")
                .generatedAt(LocalDateTime.now())
                .openaiModel("gpt-4o-mini")
                .processingTimeMs(2500L)
                .build();

        when(reportService.generateReportWithPatientInfo(sampleId, patientInfo)).thenReturn(mockReport);

        // Act & Assert
        mockMvc.perform(post("/api/reports/generate")
                        .param("sampleId", sampleId.toString())
                        .param("patientInfo", patientInfo)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sampleId").value(sampleId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.openaiModel").value("gpt-4o-mini"));

        verify(reportService).generateReportWithPatientInfo(sampleId, patientInfo);
    }

    @Test
    void testOpenAI_ShouldReturnOk_WhenConfigured() throws Exception {
        // Arrange
        when(openAIService.isConfigured()).thenReturn(true);
        when(openAIService.generateGeneticReport(any(), any())).thenReturn("Test response from OpenAI");

        // Act & Assert
        mockMvc.perform(get("/api/reports/test-openai"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("OpenAI API is working!")));
    }

    @Test
    void testOpenAI_ShouldReturnServiceUnavailable_WhenNotConfigured() throws Exception {
        // Arrange
        when(openAIService.isConfigured()).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/reports/test-openai"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().string(containsString("OpenAI service is not configured")));
    }
}