package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "AI-generated genetic report information")
public record ReportDTO(
    @Schema(description = "Unique identifier of the report")
    UUID id,
    
    @Schema(description = "Associated sample ID")
    UUID sampleId,
    
    @Schema(description = "S3 storage URL of the report")
    String s3Url,

    @Schema(description = "S3 storage URL of the patient report")
    String s3UrlPatient,
    
    @Schema(description = "Report generation timestamp")
    LocalDateTime generatedAt,
    
    @Schema(description = "OpenAI model used for generation")
    String openaiModel,
    
    @Schema(description = "Current processing status")
    ReportStatus status,
    
    @Schema(description = "Processing time in milliseconds")
    Long processingTimeMs
) {}