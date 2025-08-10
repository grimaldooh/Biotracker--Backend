package com.biotrack.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicationAnalysisDTO(
        @NotNull
        @NotBlank
        String id,
        
        @NotNull
        @NotBlank
        String name,
        
        String brand,
        String activeSubstance,
        String indication,
        String dosage,
        String frequency,
        String startDate,
        String endDate,
        
        @NotNull
        @NotBlank
        String prescribedById,
        
        String prescribedBy
        
    ) {}