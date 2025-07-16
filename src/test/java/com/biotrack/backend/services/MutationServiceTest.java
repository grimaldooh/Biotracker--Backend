package com.biotrack.backend.services;

import com.biotrack.backend.models.*;
import com.biotrack.backend.models.enums.Relevance;
import com.biotrack.backend.repositories.MutationRepository;
import com.biotrack.backend.services.impl.MutationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MutationServiceTest {

    @Mock
    private MutationRepository mutationRepository;
    
    @Mock
    private ResultFileService resultFileService;
    
    @Mock
    private S3Service s3Service;

    private MutationService mutationService;

    @BeforeEach
    void setUp() {
        mutationService = new MutationServiceImpl(mutationRepository, resultFileService, s3Service);
    }

    @Test
    void processResultFile_ShouldParseMutations_WhenValidCSV() {
        // Arrange
        UUID resultFileId = UUID.fromString("73a2e687-7401-4362-978b-e2ca46c8b1ea");
        
        Sample sample = Sample.builder()
                .id(UUID.fromString("38fa3d6b-d486-4337-bd2f-059cb1f10d6b"))
                .build();
        
        ResultFile resultFile = ResultFile.builder()
                .id(resultFileId)
                .s3Url("https://biotrack-results-files.s3.amazonaws.com/results/1752563586_1bb9148c-db73-4598-9a04-a0bc2c18dc9b_file.csv")
                .sample(sample)
                .build();

        String csvContent = """
                gene,chromosome,type,relevance,comment
                BRCA1,17,SNV,HIGH,Pathogenic variant associated with breast cancer
                TP53,17,DELETION,MEDIUM,Tumor suppressor gene variant
                CFTR,7,INSERTION,LOW,Benign variant
                """;

        InputStream csvStream = new ByteArrayInputStream(csvContent.getBytes());
        
        when(resultFileService.findById(resultFileId)).thenReturn(resultFile);
        when(s3Service.downloadFile("results/1752563586_1bb9148c-db73-4598-9a04-a0bc2c18dc9b_file.csv"))
                .thenReturn(csvStream);
        
        List<Mutation> expectedMutations = Arrays.asList(
                Mutation.builder().gene("BRCA1").chromosome("17").type("SNV").relevance(Relevance.HIGH).build(),
                Mutation.builder().gene("TP53").chromosome("17").type("DELETION").relevance(Relevance.MEDIUM).build(),
                Mutation.builder().gene("CFTR").chromosome("7").type("INSERTION").relevance(Relevance.LOW).build()
        );
        
        when(mutationRepository.saveAll(any())).thenReturn(expectedMutations);

        // Act
        List<Mutation> result = mutationService.processResultFile(resultFileId);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(s3Service).downloadFile("results/1752563586_1bb9148c-db73-4598-9a04-a0bc2c18dc9b_file.csv");
        verify(mutationRepository).saveAll(argThat(mutations -> 
                StreamSupport.stream(mutations.spliterator(), false).count() == 3 &&
                StreamSupport.stream(mutations.spliterator(), false).anyMatch(m -> "BRCA1".equals(m.getGene()))
        ));
    }

    @Test
    void processResultFile_ShouldThrowException_WhenS3DownloadFails() {
        // Arrange
        UUID resultFileId = UUID.randomUUID();
        ResultFile resultFile = ResultFile.builder()
                .id(resultFileId)
                .s3Url("https://biotrack-results-files.s3.amazonaws.com/invalid/file.csv")
                .sample(Sample.builder().id(UUID.randomUUID()).build())
                .build();

        when(resultFileService.findById(resultFileId)).thenReturn(resultFile);
        when(s3Service.downloadFile(anyString())).thenThrow(new RuntimeException("S3 download failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> mutationService.processResultFile(resultFileId));
        
        assertTrue(exception.getMessage().contains("Error reading mutation file"));
    }

    @Test
    void search_ShouldReturnFilteredMutations_WhenParametersProvided() {
        // Arrange
        UUID sampleId = UUID.fromString("38fa3d6b-d486-4337-bd2f-059cb1f10d6b");
        Relevance relevance = Relevance.HIGH;
        String gene = "BRCA1";
        
        List<Mutation> expectedMutations = Arrays.asList(
                Mutation.builder()
                        .gene("BRCA1")
                        .relevance(Relevance.HIGH)
                        .build()
        );
        
        when(mutationRepository.findBySampleIdAndRelevanceAndGeneContainingIgnoreCase(sampleId, relevance, gene))
                .thenReturn(expectedMutations);

        // Act
        List<Mutation> result = mutationService.search(sampleId, relevance, gene);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("BRCA1", result.get(0).getGene());
        verify(mutationRepository).findBySampleIdAndRelevanceAndGeneContainingIgnoreCase(sampleId, relevance, gene);
    }

    @Test
    void search_ShouldReturnAllMutations_WhenNoParametersProvided() {
        // Arrange
        List<Mutation> allMutations = Arrays.asList(
                Mutation.builder().gene("BRCA1").build(),
                Mutation.builder().gene("TP53").build()
        );
        
        when(mutationRepository.findAll()).thenReturn(allMutations);

        // Act
        List<Mutation> result = mutationService.search(null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(mutationRepository).findAll();
    }
}