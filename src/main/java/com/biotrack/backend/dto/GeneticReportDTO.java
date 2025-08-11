package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GeneticReportDTO(
    UUID reportId,
    GeneticSampleInfo geneticSample,
    SampleInfo sample,
    String s3Url,
    String s3UrlPatient
) {
    public record GeneticSampleInfo(
        UUID id,
        SampleType type,
        SampleStatus status,
        UUID medicalEntityId,
        LocalDate collectionDate,
        String notes,
        LocalDate createdAt,
        BigDecimal confidenceScore,
        String processingSoftware,
        String referenceGenome
    ) {}

    public record SampleInfo(
        UUID id,
        SampleType type,
        SampleStatus status,
        UUID medicalEntityId,
        LocalDate collectionDate,
        String notes,
        LocalDate createdAt
    ) {}
}