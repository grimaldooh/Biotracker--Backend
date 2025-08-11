package com.biotrack.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class ClinicalHistoryRecordDTO {
    private UUID id;
    private UUID patientId;
    private String s3Url;
    private String s3UrlPatient;
    private LocalDateTime createdAt;

    // Getters y setters
}