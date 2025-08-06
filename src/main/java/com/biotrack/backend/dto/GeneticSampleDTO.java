package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record GeneticSampleDTO(
        UUID id,

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

        LocalDate createdAt,

        List<MutationDTO> mutations,

        BigDecimal confidenceScore,

        String processingSoftware,

        String referenceGenome,

        Integer mutationCount
) {}