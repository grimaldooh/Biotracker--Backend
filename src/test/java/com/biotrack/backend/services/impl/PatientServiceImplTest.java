package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.enums.Gender;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.services.impl.PatientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PatientServiceImplTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PatientServiceImpl patientService;

    private Patient testPatient;
    private UUID testPatientId;

    @BeforeEach
    void setUp() {
        testPatientId = UUID.randomUUID();
        testPatient = Patient.builder()
                .id(testPatientId)
                .firstName("John")
                .lastName("Doe")
                .email("john.patient@test.com")
                .password("password123")
                .phoneNumber("1234567890")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .curp("CURP123456HDFRRN09")
                .createdAt(LocalDate.now())
                .build();
    }

    @Test
    void create_ShouldReturnSavedPatient() {
        // Given
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(patientRepository.save(any(Patient.class))).thenReturn(testPatient);

        // When
        Patient result = patientService.create(testPatient);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testPatientId);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john.patient@test.com");
        assertThat(result.getGender()).isEqualTo(Gender.MALE);
        
        verify(passwordEncoder, times(1)).encode("password123");
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void findAll_ShouldReturnListOfPatients() {
        // Given
        Patient anotherPatient = Patient.builder()
                .id(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.patient@test.com")
                .gender(Gender.FEMALE)
                .build();

        List<Patient> expectedPatients = Arrays.asList(testPatient, anotherPatient);
        when(patientRepository.findAll()).thenReturn(expectedPatients);

        // When
        List<Patient> result = patientService.findAll();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(testPatient, anotherPatient);
        
        verify(patientRepository, times(1)).findAll();
    }

    @Test
    void findById_WithExistingId_ShouldReturnPatient() {
        // Given
        when(patientRepository.findById(testPatientId)).thenReturn(Optional.of(testPatient));

        // When
        Patient result = patientService.findById(testPatientId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testPatientId);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        
        verify(patientRepository, times(1)).findById(testPatientId);
    }

    @Test
    void findById_WithNonExistingId_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(patientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> patientService.findById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Patient not found");
        
        verify(patientRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void update_WithExistingPatient_ShouldReturnUpdatedPatient() {
        // Given
        Patient updatedPatientData = Patient.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.patient@test.com")
                .phoneNumber("0987654321")
                .gender(Gender.FEMALE)
                .build();

        Patient existingPatient = Patient.builder()
                .id(testPatientId)
                .firstName("John")
                .lastName("Doe")
                .email("john.patient@test.com")
                .phoneNumber("1234567890")
                .gender(Gender.MALE)
                .build();

        Patient expectedUpdatedPatient = Patient.builder()
                .id(testPatientId)
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.patient@test.com")
                .phoneNumber("0987654321")
                .gender(Gender.FEMALE)
                .build();

        when(patientRepository.findById(testPatientId)).thenReturn(Optional.of(existingPatient));
        when(patientRepository.save(any(Patient.class))).thenReturn(expectedUpdatedPatient);

        // When
        Patient result = patientService.update(testPatientId, updatedPatientData);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testPatientId);
        assertThat(result.getFirstName()).isEqualTo("Jane");
        assertThat(result.getLastName()).isEqualTo("Smith");
        assertThat(result.getGender()).isEqualTo(Gender.FEMALE);
        
        verify(patientRepository, times(1)).findById(testPatientId);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void update_WithNonExistingPatient_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        Patient updatedPatientData = Patient.builder()
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(patientRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> patientService.update(nonExistentId, updatedPatientData))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Patient not found");
        
        verify(patientRepository, times(1)).findById(nonExistentId);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    @Test
    void deleteById_WithExistingPatient_ShouldDeleteSuccessfully() {
        // Given
        when(patientRepository.existsById(testPatientId)).thenReturn(true);
        doNothing().when(patientRepository).deleteById(testPatientId);

        // When
        patientService.deleteById(testPatientId);

        // Then
        verify(patientRepository, times(1)).existsById(testPatientId);
        verify(patientRepository, times(1)).deleteById(testPatientId);
    }

    @Test
    void deleteById_WithNonExistingPatient_ShouldThrowException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(patientRepository.existsById(nonExistentId)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> patientService.deleteById(nonExistentId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Patient not found");
        
        verify(patientRepository, times(1)).existsById(nonExistentId);
        verify(patientRepository, never()).deleteById(nonExistentId);
    }

    @Test
    void searchPatients_ShouldReturnMatchingPatients() {
        // Given
        String firstName = "John";
        String lastName = "Doe";
        List<Patient> expectedPatients = Arrays.asList(testPatient);
        when(patientRepository.findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName))
                .thenReturn(expectedPatients);

        // When
        List<Patient> result = patientService.searchPatients(firstName, lastName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(0).getLastName()).isEqualTo("Doe");
        
        verify(patientRepository, times(1))
                .findByFirstNameContainingIgnoreCaseAndLastNameContainingIgnoreCase(firstName, lastName);
    }

    @Test
    void create_WithNullPatient_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> patientService.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Patient cannot be null");
        
        verify(patientRepository, never()).save(any(Patient.class));
    }
}
