package com.biotrack.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResultFileDTO (
        UUID id,
        String fileName,
        String s3Url,
        LocalDateTime uploadedAt,
        UUID sampleId
){}




