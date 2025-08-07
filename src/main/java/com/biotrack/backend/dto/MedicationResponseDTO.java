package com.biotrack.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MedicationResponseDTO(
        UUID id,
        String name,
        String brand,
        String activeSubstance,
        String indication,
        String dosage,
        String frequency,
        LocalDate startDate,
        LocalDate endDate,
        UUID prescribedById,
        String prescribedByName, // Nombre del doctor que lo prescribió
        UUID patientId
) {}