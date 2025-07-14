package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record SampleDTO(
        UUID id,

        @NotNull
        UUID patientId,

        @NotNull
        UUID registeredById,

        @NotNull
        SampleType type,

        @NotNull
        SampleStatus status,

        LocalDate collectionDate,

        String notes
) {}