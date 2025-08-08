package com.biotrack.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record MedicationCompatibilityRequestDTO(
        @NotNull
        UUID patientId,
        
        @NotNull
        @NotEmpty
        @Valid
        List<MedicationAnalysisDTO> medications
) {}