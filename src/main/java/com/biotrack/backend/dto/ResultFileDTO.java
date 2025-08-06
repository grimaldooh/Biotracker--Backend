package com.biotrack.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResultFileDTO(
        UUID id,
        String fileName,
        String s3Key,
        String s3Url,
        Long fileSize,
        String contentType,
        String processingStatus,
        LocalDateTime uploadedAt,
        UUID geneticSampleId // ✅ CAMBIAR: de sampleId a geneticSampleId
) {}




