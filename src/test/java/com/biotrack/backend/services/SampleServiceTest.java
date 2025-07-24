package com.biotrack.backend.services;

import com.biotrack.backend.models.DnaSample;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;
import com.biotrack.backend.repositories.ReportRepository;
import com.biotrack.backend.repositories.SampleRepository;
import com.biotrack.backend.services.impl.SampleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SampleServiceTest {

    private SampleRepository sampleRepository;
    private ReportRepository reportRepository;
    private SampleService sampleService;

    @BeforeEach
    void setUp() {
        sampleRepository = mock(SampleRepository.class);
        reportRepository = mock(ReportRepository.class);
        sampleService = new SampleServiceImpl(sampleRepository, reportRepository);
    }

    @Test
    void shouldCreateSample() {
        Sample sample = createSample();

        when(sampleRepository.save(any(Sample.class))).thenReturn(sample);

        Sample result = sampleService.create(sample);

        assertNotNull(result);
        assertEquals(SampleType.DNA, result.getType());
        verify(sampleRepository, times(1)).save(sample);
    }

    @Test
    void shouldFindAllSamples() {
        when(sampleRepository.findAll()).thenReturn(Arrays.asList(createSample()));

        List<Sample> result = sampleService.findAll();

        assertEquals(1, result.size());
        verify(sampleRepository, times(1)).findAll();
    }

    @Test
    void shouldFindSampleById() {
        Sample sample = createSample();
        UUID id = sample.getId();

        when(sampleRepository.findById(id)).thenReturn(Optional.of(sample));

        Sample result = sampleService.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void shouldDeleteSampleById() {
        UUID id = UUID.randomUUID();

        sampleService.deleteById(id);

        verify(sampleRepository, times(1)).deleteById(id);
    }

    private Sample createSample() {
        return DnaSample.builder()
                .id(UUID.randomUUID())
                .patient(mock(Patient.class))
                .registeredBy(mock(User.class))
                .type(SampleType.DNA)
                .status(SampleStatus.PENDING)
                .collectionDate(LocalDate.now())
                .createdAt(LocalDate.now())
                .notes("Test sample")
                .build();
    }
}
