package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.LabAppointmentStatus;
import com.biotrack.backend.models.enums.SampleType;
import java.util.UUID;

public record LabAppointmentCreationDTO(
    UUID doctorId,
    UUID patientId,
    UUID medicalEntityId,
    SampleType sampleType,
    String notes
) {}