
package com.biotrack.backend.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.biotrack.backend.dto.BloodSampleDataDTO;
import com.biotrack.backend.dto.DnaSampleDataDTO;
import com.biotrack.backend.dto.SalivaSampleDataDTO;
import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;

import jakarta.validation.constraints.NotNull;

public record SampleCreationDTO(
    UUID patientId,
    UUID registeredById,
    SampleType type,
    SampleStatus status,
    LocalDate collectionDate,
    String notes,
    UUID doctorReferedId,
    UUID medicalEntityId,
    BloodSampleDataDTO bloodData,
    DnaSampleDataDTO dnaData,
    SalivaSampleDataDTO salivaData
) {}