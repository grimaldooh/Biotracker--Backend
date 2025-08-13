package com.biotrack.backend.utils;

import com.biotrack.backend.models.User;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.Hospital;
import com.biotrack.backend.models.Medication;
import com.biotrack.backend.models.enums.Role;
import com.biotrack.backend.models.enums.Gender;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Utility class for creating test objects with realistic data
 */
public class TestDataFactory {

    public static User createTestUser() {
        return createTestUser("Test User", "test@example.com", Role.MEDIC);
    }

    public static User createTestUser(String name, String email, Role role) {
        return User.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .phoneNumber("1234567890")
                .password("password123")
                .role(role)
                .specialty("General Medicine")
                .createdAt(LocalDate.now())
                .build();
    }

    public static Patient createTestPatient() {
        return createTestPatient("John", "Doe", "john.doe@patient.com");
    }

    public static Patient createTestPatient(String firstName, String lastName, String email) {
        return Patient.builder()
                .id(UUID.randomUUID())
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password("password123")
                .phoneNumber("0987654321")
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .curp("CURP123456HDFRRN09")
                .createdAt(LocalDate.now())
                .build();
    }

    public static Hospital createTestHospital() {
        return createTestHospital("Test Hospital", "Test City");
    }

    public static Hospital createTestHospital(String name, String location) {
        return Hospital.builder()
                .id(UUID.randomUUID())
                .name(name)
                .location(location)
                .fullAddress("123 Test Street, Test City, Test State")
                .legalIdentifier("RFC123456789")
                .build();
    }

    public static Medication createTestMedication() {
        return Medication.builder()
                .name("Acetaminophen")
                .brand("Tylenol")
                .activeSubstance("Acetaminophen")
                .indication("Pain relief and fever reduction")
                .dosage("500mg")
                .frequency("Every 6 hours")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(7))
                .build();
    }

    public static User createAdminUser() {
        return createTestUser("Admin User", "admin@example.com", Role.ADMIN);
    }

    public static User createMedicUser() {
        return createTestUser("Dr. Smith", "dr.smith@example.com", Role.MEDIC);
    }

    public static User createLabTechnicianUser() {
        return createTestUser("Lab Tech", "lab@example.com", Role.LAB_TECHNICIAN);
    }

    public static Patient createFemalePatient() {
        return Patient.builder()
                .id(UUID.randomUUID())
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@patient.com")
                .password("password123")
                .phoneNumber("5555555555")
                .birthDate(LocalDate.of(1992, 5, 15))
                .gender(Gender.FEMALE)
                .curp("CURP789012MDFRRN05")
                .createdAt(LocalDate.now())
                .build();
    }
}
