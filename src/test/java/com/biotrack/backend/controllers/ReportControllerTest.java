package com.biotrack.backend.controllers;

import com.biotrack.backend.dto.ReportDTO;
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
    void generateReport_ShouldReturnInternalServerError_WhenServiceThrowsException() throws Exception {
        // Arrange
        UUID sampleId = UUID.fromString("38fa3d6b-d486-4337-bd2f-059cb1f10d6b");
        
        when(reportService.generateReportWithPatientInfo(eq(sampleId), any()))
                .thenThrow(new RuntimeException("OpenAI service unavailable"));

        // Act & Assert
        mockMvc.perform(post("/api/reports/generate")
                        .param("sampleId", sampleId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("OpenAI service unavailable")));
    }

    @Test
    void getAllReports_ShouldReturnReportList() throws Exception {
        // Arrange
        List<Report> mockReports = Arrays.asList(
                Report.builder()
                        .id(UUID.randomUUID())
                        .sample(Sample.builder().id(UUID.randomUUID()).build())
                        .status(ReportStatus.COMPLETED)
                        .generatedAt(LocalDateTime.now())
                        .build(),
                Report.builder()
                        .id(UUID.randomUUID())
                        .sample(Sample.builder().id(UUID.randomUUID()).build())
                        .status(ReportStatus.FAILED)
                        .generatedAt(LocalDateTime.now())
                        .build()
        );

        when(reportService.findByStatus(null)).thenReturn(mockReports);

        // Act & Assert
        mockMvc.perform(get("/api/reports")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[1].status").value("FAILED"));
    }

    @Test
    void getReportsBySample_ShouldReturnSampleReports() throws Exception {
        // Arrange
        UUID sampleId = UUID.fromString("38fa3d6b-d486-4337-bd2f-059cb1f10d6b");
        
        List<Report> mockReports = Arrays.asList(
                Report.builder()
                        .id(UUID.randomUUID())
                        .sample(Sample.builder().id(sampleId).build())
                        .status(ReportStatus.COMPLETED)
                        .generatedAt(LocalDateTime.now())
                        .build()
        );

        when(reportService.findBySampleId(sampleId)).thenReturn(mockReports);

        // Act & Assert
        mockMvc.perform(get("/api/reports/sample/{sampleId}", sampleId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sampleId").value(sampleId.toString()));
    }

    @Test
    void getReportById_ShouldReturnReport_WhenExists() throws Exception {
        // Arrange
        UUID reportId = UUID.randomUUID();
        UUID sampleId = UUID.fromString("38fa3d6b-d486-4337-bd2f-059cb1f10d6b");
        
        Report mockReport = Report.builder()
                .id(reportId)
                .sample(Sample.builder().id(sampleId).build())
                .status(ReportStatus.COMPLETED)
                .s3Url("https://biotrack-results-files.s3.amazonaws.com/reports/test.txt")
                .generatedAt(LocalDateTime.now())
                .openaiModel("gpt-4o-mini")
                .build();

        when(reportService.findById(reportId)).thenReturn(mockReport);

        // Act & Assert
        mockMvc.perform(get("/api/reports/{reportId}", reportId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.sampleId").value(sampleId.toString()))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void deleteReport_ShouldReturnNoContent_WhenSuccessful() throws Exception {
        // Arrange
        UUID reportId = UUID.randomUUID();
        doNothing().when(reportService).deleteReport(reportId);

        // Act & Assert
        mockMvc.perform(delete("/api/reports/{reportId}", reportId))
                .andExpect(status().isNoContent());

        verify(reportService).deleteReport(reportId);
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