package com.biotrack.backend.services;

import com.biotrack.backend.models.*;
import com.biotrack.backend.models.enums.*;
import com.biotrack.backend.repositories.MutationRepository;
import com.biotrack.backend.repositories.ReportRepository;
import com.biotrack.backend.services.impl.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;
    
    @Mock
    private MutationRepository mutationRepository;
    
    @Mock
    private SampleService sampleService;
    
    @Mock
    private OpenAIService openAIService;
    
    @Mock
    private S3Service s3Service;

    private ReportService reportService;
    
    // Datos de prueba basados en tu BD
    private Patient testPatient;
    private User testUser;
    private DnaSample testSample;
    private List<Mutation> testMutations;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl(
                reportRepository,
                mutationRepository,
                sampleService,
                openAIService,
                s3Service
        );
        
        setupTestData();
    }

    private void setupTestData() {
        // Paciente de prueba (datos reales de tu BD)
        testPatient = Patient.builder()
                .id(UUID.fromString("0ee3f077-13d5-4d6f-8298-78da76da5343"))
                .firstName("Juan")
                .lastName("Pérez")
                .birthDate(LocalDate.of(1990, 5, 10))
                .gender(Gender.MALE)
                .curp("JUPE900510HBCXXX01")
                .build();

        // Usuario de prueba (datos reales de tu BD)
        testUser = User.builder()
                .id(UUID.fromString("5b574fad-c59a-4184-bdee-696295f142df"))
                .name("Dr. Ángel")
                .email("angel@biotrack.ai")
                .password("123456")
                .role(Role.TECHNICIAN)
                .build();

        // Muestra de prueba (datos reales de tu BD)
        testSample = DnaSample.builder()
                .id(UUID.fromString("38fa3d6b-d486-4337-bd2f-059cb1f10d6b"))
                .patient(testPatient)
                .registeredBy(testUser)
                .type(SampleType.DNA)
                .status(SampleStatus.PENDING)
                .collectionDate(LocalDate.of(2025, 7, 14))
                .notes("Test file upload")
                .build();

        // Mutaciones de prueba
        testMutations = Arrays.asList(
                Mutation.builder()
                        .id(UUID.randomUUID())
                        .gene("BRCA1")
                        .chromosome("17")
                        .type("SNV")
                        .relevance(Relevance.HIGH)
                        .comment("Pathogenic variant")
                        .sample(testSample)
                        .build(),
                Mutation.builder()
                        .id(UUID.randomUUID())
                        .gene("TP53")
                        .chromosome("17")
                        .type("DELETION")
                        .relevance(Relevance.MEDIUM)
                        .comment("Likely pathogenic")
                        .sample(testSample)
                        .build()
        );
    }

    @Test
    void generateReport_ShouldCreateCompletedReport_WhenAllServicesWork() {
        // Arrange
        UUID sampleId = testSample.getId();
        String expectedReportContent = "Generated AI genetic report content";
        String expectedS3Url = "https://biotrack-results-files.s3.amazonaws.com/reports/12345_report.txt";
        
        when(sampleService.findById(sampleId)).thenReturn(testSample);
        when(openAIService.isConfigured()).thenReturn(true);
        when(openAIService.getModelUsed()).thenReturn("gpt-4o-mini");
        when(reportRepository.existsBySampleIdAndStatus(sampleId, ReportStatus.GENERATING)).thenReturn(false);
        when(mutationRepository.findBySampleId(sampleId)).thenReturn(testMutations);
        when(openAIService.generateGeneticReport(any(), any())).thenReturn(expectedReportContent);
        when(s3Service.uploadTextContent(any(), any())).thenReturn(expectedS3Url);
        
        Report savedReport = Report.builder()
                .id(UUID.randomUUID())
                .sample(testSample)
                .status(ReportStatus.COMPLETED)
                .s3Url(expectedS3Url)
                .generatedAt(LocalDateTime.now())
                .openaiModel("gpt-4o-mini")
                .processingTimeMs(1500L)
                .build();
        
        when(reportRepository.save(any(Report.class))).thenReturn(savedReport);

        // Act
        Report result = reportService.generateReport(sampleId);

        // Assert
        assertNotNull(result);
        assertEquals(ReportStatus.COMPLETED, result.getStatus());
        assertEquals(expectedS3Url, result.getS3Url());
        assertEquals("gpt-4o-mini", result.getOpenaiModel());
        assertEquals(testSample.getId(), result.getSample().getId());
        
        verify(openAIService).generateGeneticReport(eq(testMutations), contains("Juan Pérez"));
        verify(s3Service).uploadTextContent(eq(expectedReportContent), anyString());
        verify(reportRepository, times(2)).save(any(Report.class));
    }

    @Test
    void generateReport_ShouldThrowException_WhenOpenAINotConfigured() {
        // Arrange
        UUID sampleId = testSample.getId();
        when(sampleService.findById(sampleId)).thenReturn(testSample);
        when(openAIService.isConfigured()).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> reportService.generateReport(sampleId));
        
        assertTrue(exception.getMessage().contains("OpenAI service is not configured"));
        verify(reportRepository, never()).save(any());
    }

    @Test
    void generateReport_ShouldThrowException_WhenNoMutationsFound() {
        // Arrange
        UUID sampleId = testSample.getId();
        when(sampleService.findById(sampleId)).thenReturn(testSample);
        when(openAIService.isConfigured()).thenReturn(true);
        when(reportRepository.existsBySampleIdAndStatus(sampleId, ReportStatus.GENERATING)).thenReturn(false);
        when(mutationRepository.findBySampleId(sampleId)).thenReturn(List.of());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> reportService.generateReport(sampleId));
        
        assertTrue(exception.getMessage().contains("No mutations found for sample"));
    }

    @Test
    void findBySampleId_ShouldReturnReports_WhenSampleExists() {
        // Arrange
        UUID sampleId = testSample.getId();
        List<Report> expectedReports = Arrays.asList(
                Report.builder().id(UUID.randomUUID()).sample(testSample).status(ReportStatus.COMPLETED).build(),
                Report.builder().id(UUID.randomUUID()).sample(testSample).status(ReportStatus.FAILED).build()
        );
        
        when(reportRepository.findBySampleIdOrderByGeneratedAtDesc(sampleId)).thenReturn(expectedReports);

        // Act
        List<Report> result = reportService.findBySampleId(sampleId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(expectedReports, result);
    }

    @Test
    void deleteReport_ShouldRemoveFromS3AndDB_WhenReportExists() {
        // Arrange
        UUID reportId = UUID.randomUUID();
        String s3Key = "reports/12345_report.txt";
        
        Report report = Report.builder()
                .id(reportId)
                .sample(testSample)
                .s3Key(s3Key)
                .status(ReportStatus.COMPLETED)
                .build();
        
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));

        // Act
        reportService.deleteReport(reportId);

        // Assert
        verify(s3Service).deleteFile(s3Key);
        verify(reportRepository).delete(report);
    }
}