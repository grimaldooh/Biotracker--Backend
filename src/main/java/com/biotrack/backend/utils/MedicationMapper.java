package com.biotrack.backend.utils;

import com.biotrack.backend.dto.MedicationResponseDTO;
import com.biotrack.backend.models.Medication;

public class MedicationMapper {
    
    public static MedicationResponseDTO toResponseDTO(Medication medication) {
        return new MedicationResponseDTO(
                medication.getId(),
                medication.getName(),
                medication.getBrand(),
                medication.getActiveSubstance(),
                medication.getIndication(),
                medication.getDosage(),
                medication.getFrequency(),
                medication.getStartDate(),
                medication.getEndDate(),
                medication.getPrescribedBy() != null ? medication.getPrescribedBy().getId() : null,
                medication.getPrescribedBy() != null ? 
                    medication.getPrescribedBy().getName() : null,
                medication.getPatient().getId()
        );
    }
}