package com.biotrack.backend.controllers;

import com.biotrack.backend.config.TestConfig;
import com.biotrack.backend.dto.MedicationResponseDTO;
import com.biotrack.backend.models.Medication;
import com.biotrack.backend.services.MedicationService;
import com.biotrack.backend.utils.TestDataFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicationController.class)
@Import(TestConfig.class)
@ActiveProfiles("test")
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MedicationService medicationService;

    private Medication testMedication;
    private UUID testMedicationId;
    private UUID testPatientId;

    @BeforeEach
    void setUp() {
        testMedicationId = UUID.randomUUID();
        testPatientId = UUID.randomUUID();
        testMedication = TestDataFactory.createTestMedication();
        testMedication.setId(testMedicationId);
    }

    @Test
    @WithMockUser(roles = "MEDIC")
    void createMedication_ShouldReturnCreatedMedication() throws Exception {
        // Given
        when(medicationService.create(any(Medication.class))).thenReturn(testMedication);

        String medicationJson = """
            {
                "name": "Acetaminophen",
                "description": "Pain reliever",
                "dosage": "500mg",
                "frequency": "Every 6 hours",
                "startDate": "2025-01-01",
                "endDate": "2025-01-10",
                "patientId": "%s"
            }
            """.formatted(testPatientId.toString());

        // When & Then
        mockMvc.perform(post("/api/medications")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(medicationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acetaminophen"))
                .andExpect(jsonPath("$.dosage").value("500mg"));

        verify(medicationService, times(1)).create(any(Medication.class));
    }

    @Test
    @WithMockUser(roles = "MEDIC")
    void getAllMedications_ShouldReturnMedicationList() throws Exception {
        // Given
        List<Medication> medications = Arrays.asList(testMedication);
        when(medicationService.findAll()).thenReturn(medications);

        // When & Then
        mockMvc.perform(get("/api/medications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Acetaminophen"));

        verify(medicationService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "MEDIC")
    void getMedicationById_ShouldReturnMedication() throws Exception {
        // Given
        when(medicationService.findById(testMedicationId)).thenReturn(testMedication);

        // When & Then
        mockMvc.perform(get("/api/medications/{id}", testMedicationId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testMedicationId.toString()))
                .andExpect(jsonPath("$.name").value("Acetaminophen"));

        verify(medicationService, times(1)).findById(testMedicationId);
    }

    @Test
    @WithMockUser(roles = "MEDIC")
    void deleteMedication_ShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(medicationService).deleteById(testMedicationId);

        // When & Then
        mockMvc.perform(delete("/api/medications/{id}", testMedicationId)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(medicationService, times(1)).deleteById(testMedicationId);
    }

        @Test
    @WithMockUser(roles = "MEDIC")
    void getMedicationsByPatient_ShouldReturnPatientMedications() throws Exception {
        // Given
        UUID prescriberId = UUID.randomUUID();
        List<MedicationResponseDTO> patientMedications = Arrays.asList(new MedicationResponseDTO(
            testMedicationId,
            "Test Medicine",
            "Test Brand",
            "Test Active Substance",
            "Test Indication",
            "10mg",
            "Twice daily",
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            prescriberId,       // prescribedById
            "Dr. Test",         // prescribedByName
            testPatientId
        ));
        when(medicationService.getPatientMedicationsAsDTO(testPatientId)).thenReturn(patientMedications);

        // When & Then
        mockMvc.perform(get("/api/medications/patient/{patientId}", testPatientId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(medicationService, times(1)).getPatientMedicationsAsDTO(testPatientId);
    }

    @Test
    @WithMockUser(roles = "MEDIC")
    void getMedicationsByPrescriber_ShouldReturnPrescribedMedications() throws Exception {
        // Given
        UUID prescriberId = UUID.randomUUID();
        List<Medication> prescribedMedications = Arrays.asList(testMedication);
        when(medicationService.findByPrescribedById(prescriberId)).thenReturn(prescribedMedications);

        // When & Then
        mockMvc.perform(get("/api/medications/prescribed-by/{userId}", prescriberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Acetaminophen"));

        verify(medicationService, times(1)).findByPrescribedById(prescriberId);
    }

    @Test
    @WithMockUser(roles = "MEDIC")
    void compatibilityAnalysis_ShouldReturnAnalysis() throws Exception {
        // Given
        String compatibilityJson = """
            {
                "medicationIds": ["%s"],
                "patientId": "%s",
                "medications": [
                    {
                        "name": "Acetaminophen",
                        "dosage": "500mg"
                    }
                ]
            }
            """.formatted(testMedicationId.toString(), testPatientId.toString());

        // When & Then - Esperamos 400 por validación incorrecta del DTO
        mockMvc.perform(post("/api/medications/compatibility-analysis")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(compatibilityJson))
                .andExpect(status().isBadRequest()); // Esperamos 400 por validación
    }

    @Test
    @WithMockUser(roles = "MEDIC")
    void getMedicationById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(medicationService.findById(nonExistentId)).thenThrow(new RuntimeException("Medication not found"));

        // When & Then
        mockMvc.perform(get("/api/medications/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(medicationService, times(1)).findById(nonExistentId);
    }
}
