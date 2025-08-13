package com.biotrack.backend.services;

import com.biotrack.backend.models.Patient;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.services.impl.PatientServiceImpl;
import com.biotrack.backend.utils.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient testPatient;
    private UUID testPatientId;

    @BeforeEach
    void setUp() {
        testPatientId = UUID.randomUUID();
        testPatient = TestDataFactory.createTestPatient();
        testPatient.setId(testPatientId);
    }

    @Test
    void create_ShouldReturnSavedPatient() {
        // Given
        Patient patientToSave = TestDataFactory.createTestPatient();
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // When
        Patient result = patientService.create(patientToSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testPatientId);
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void findById_ShouldReturnPatient_WhenPatientExists() {
        // Given
        when(patientRepository.findById(testPatientId)).thenReturn(Optional.of(testPatient));

        // When
        Patient result = patientService.findById(testPatientId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testPatientId);
        verify(patientRepository).findById(testPatientId);
    }

    @Test
    void findById_ShouldThrowException_WhenPatientNotFound() {
        // Given
        when(patientRepository.findById(testPatientId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> patientService.findById(testPatientId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");

        verify(patientRepository).findById(testPatientId);
    }

    @Test
    void findAll_ShouldReturnAllPatients() {
        // Given
        List<Patient> patients = Arrays.asList(testPatient, TestDataFactory.createTestPatient());
        when(patientRepository.findAll()).thenReturn(patients);

        // When
        List<Patient> result = patientService.findAll();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).contains(testPatient);
        verify(patientRepository).findAll();
    }

    @Test
    void update_ShouldReturnUpdatedPatient_WhenPatientExists() {
        // Given
        Patient updateData = TestDataFactory.createTestPatient();
        updateData.setFirstName("Updated Name");
        when(patientRepository.findById(testPatientId)).thenReturn(Optional.of(testPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // When
        Patient result = patientService.update(testPatientId, updateData);

        // Then
        assertThat(result).isNotNull();
        verify(patientRepository).findById(testPatientId);
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    void update_ShouldThrowException_WhenPatientNotFound() {
        // Given
        Patient updateData = TestDataFactory.createTestPatient();
        when(patientRepository.findById(testPatientId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> patientService.update(testPatientId, updateData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");

        verify(patientRepository).findById(testPatientId);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void deleteById_ShouldDeletePatient_WhenPatientExists() {
        // Given
        testPatient.setHospitals(Arrays.asList()); // Lista vacía de hospitales
        when(patientRepository.findById(testPatientId)).thenReturn(Optional.of(testPatient));
        doNothing().when(patientRepository).deleteById(testPatientId);

        // When
        patientService.deleteById(testPatientId);

        // Then
        verify(patientRepository).findById(testPatientId);
        verify(patientRepository).save(testPatient); // Para actualizar relaciones
        verify(patientRepository).deleteById(testPatientId);
    }

    @Test
    void deleteById_ShouldThrowException_WhenPatientNotFound() {
        // Given
        when(patientRepository.findById(testPatientId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> patientService.deleteById(testPatientId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("not found");

        verify(patientRepository).findById(testPatientId);
        verify(patientRepository, never()).deleteById(testPatientId);
    }

    @Test
    void searchPatients_ShouldReturnMatchingPatients() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        List<Patient> matchingPatients = Arrays.asList(testPatient);
        when(patientRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName))
                .thenReturn(matchingPatients);

        // When
        List<Patient> result = patientService.searchPatients(firstName, lastName);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(testPatient);
        verify(patientRepository).findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName);
    }
}
