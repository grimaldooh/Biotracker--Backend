package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.Relevance;

import java.util.UUID;

public record MutationDTO(
        UUID id,
        String gene,
        String chromosome,
        String type,
        Relevance relevance,
        String comment,
        UUID sampleId
) {
}
