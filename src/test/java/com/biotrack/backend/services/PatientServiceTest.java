package com.biotrack.backend.services;

import com.biotrack.backend.models.Patient;
import com.biotrack.backend.repositories.ClinicalHistoryRecordRepository;
import com.biotrack.backend.repositories.MedicalVisitRepository;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.repositories.ReportRepository;
import com.biotrack.backend.services.aws.S3ServiceImpl;
import com.biotrack.backend.services.impl.OpenAIServiceImpl;
import com.biotrack.backend.services.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PatientServiceTest {

    private PatientRepository repository;
    private PatientService service;
    private MedicalVisitRepository medicalVisitRepository;
    private  ReportRepository reportRepository;
    private  S3ServiceImpl s3Service;
    private  OpenAIServiceImpl openAIService;
    private  ClinicalHistoryRecordRepository clinicalHistoryRecordRepository;

    @BeforeEach
    void setup() {
        repository = mock(PatientRepository.class);
        service = new PatientServiceImpl( repository, medicalVisitRepository,
                reportRepository, s3Service, openAIService, clinicalHistoryRecordRepository
        );
    }

    @Test
    void shouldCreatePatient() {
        Patient patient = Patient.builder().firstName("Angel").build();
        when(repository.save(any(Patient.class))).thenReturn(patient);

        Patient saved = service.create(patient);

        assertNotNull(saved);
        assertEquals("Angel", saved.getFirstName());
        verify(repository, times(1)).save(patient);
    }

    @Test
    void shouldUpdatePatient() {
        Patient existing = Patient.builder().id(UUID.randomUUID()).firstName("Angel").build();
        Patient updated = Patient.builder().id(existing.getId()).firstName("Updated").build();

        when(repository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(repository.save(any(Patient.class))).thenReturn(updated);

        Patient result = service.update(existing.getId(), updated);

        assertEquals("Updated", result.getFirstName());
    }


}