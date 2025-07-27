package com.biotrack.backend.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.biotrack.backend.models.enums.MedicalVisitType;

public record MedicalVisitDTO(
    UUID id,
    String patientName,
    String doctorName,
    LocalDateTime visitDate,
    String notes,
    String diagnosis,
    String recommendations,
    UUID medicalEntityId,
    boolean visitCompleted,
    MedicalVisitType type,
    String medicalArea
) {}
