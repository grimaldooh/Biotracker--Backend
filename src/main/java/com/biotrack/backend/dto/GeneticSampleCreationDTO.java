package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GeneticSampleCreationDTO(
        @NotNull
        UUID patientId,

        @NotNull
        UUID registeredById,

        @NotNull
        SampleType type,

        @NotNull
        SampleStatus status,

        UUID medicalEntityId,

        LocalDate collectionDate,

        String notes,

        BigDecimal confidenceScore,

        String processingSoftware,

        String referenceGenome
) {}