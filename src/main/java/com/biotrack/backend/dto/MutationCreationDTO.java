package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.Relevance;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MutationCreationDTO(
        @NotBlank(message = "Gene name is required")
        String gene,

        @NotBlank(message = "Chromosome is required")
        String chromosome,

        @NotBlank(message = "Mutation type is required")
        String type,

        @NotNull(message = "Relevance is required")
        Relevance relevance,

        String comment
) {}