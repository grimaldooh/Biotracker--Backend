package com.biotrack.backend.services;

import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.enums.Gender;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.services.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PatientServiceTest {

    private PatientRepository repository;
    private PatientService service;

    @BeforeEach
    void setup() {
        repository = mock(PatientRepository.class);
        service = new PatientServiceImpl(repository);
    }

    @Test
    void shouldCreatePatient() {
        Patient patient = createPatient();

        when(repository.save(any(Patient.class))).thenReturn(patient);

        Patient saved = service.create(patient);

        assertNotNull(saved);
        assertEquals("Angel", saved.getFirstName());
        verify(repository, times(1)).save(patient);
    }

    @Test
    void shouldUpdatePatient() {
        Patient existing = createPatient();
        UUID id = existing.getId();
        Patient updated = createPatient();
        updated.setFirstName("Updated");

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(Patient.class))).thenReturn(updated);

        Patient result = service.update(id, updated);

        assertEquals("Updated", result.getFirstName());
    }

    private Patient createPatient() {
        return Patient.builder()
                .id(UUID.randomUUID())
                .firstName("Angel")
                .lastName("Dev")
                .birthDate(LocalDate.of(1999, 1, 1))
                .gender(Gender.MALE)
                .curp("ANG9990101HBCXXX01")
                .createdAt(LocalDate.now())
                .build();
    }
}