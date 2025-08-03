package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.LabAppointmentStatus;
import com.biotrack.backend.models.enums.SampleType;
import java.time.LocalDateTime;
import java.util.UUID;

public record LabAppointmentDTO(
    UUID id,
    UUID medicalEntityId,
    UUID doctorId,
    UUID patientId,
    LocalDateTime createdAt,
    LabAppointmentStatus status,
    SampleType sampleType,
    String notes
) {}