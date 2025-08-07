package com.biotrack.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MedicationPatchDTO(
        @NotNull
        @NotEmpty
        @Valid
        List<MedicationOperationDTO> operations
) {}