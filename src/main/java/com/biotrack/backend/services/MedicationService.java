package com.biotrack.backend.services;

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
}