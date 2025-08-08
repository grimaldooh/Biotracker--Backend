package com.biotrack.backend.services;

import com.biotrack.backend.dto.MedicationAnalysisDTO;
import com.biotrack.backend.dto.MedicationOperationDTO;
import com.biotrack.backend.dto.MedicationPatchDTO;
import com.biotrack.backend.dto.MedicationResponseDTO;
import com.biotrack.backend.models.Medication;

import java.util.List;
import java.util.UUID;

public interface MedicationService {
    Medication create(Medication medication);
    List<Medication> findAll();
    Medication findById(UUID id);
    void deleteById(UUID id);
    List<Medication> findByPatientId(UUID patientId);
    List<Medication> findByPrescribedById(UUID userId);
    
    List<MedicationResponseDTO> patchPatientMedications(UUID patientId, MedicationPatchDTO patchDTO);
    
    List<MedicationResponseDTO> getPatientMedicationsAsDTO(UUID patientId);
    
    /**
     * Genera un reporte de compatibilidad de medicamentos para un paciente
     * @param patientId ID del paciente
     * @param medications Lista de medicamentos a analizar
     * @return Contenido JSON del reporte de compatibilidad generado por OpenAI
     * @throws RuntimeException si el paciente no existe, OpenAI no está configurado, 
     *                         o no se pueden analizar los medicamentos
     */
    String generateCompatibilityReport(UUID patientId, List<MedicationAnalysisDTO> medications);
}