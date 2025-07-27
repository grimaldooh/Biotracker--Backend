package com.biotrack.backend.dto.Samples;

import com.biotrack.backend.dto.BloodSampleDataDTO;
import com.biotrack.backend.dto.DnaSampleDataDTO;
import com.biotrack.backend.dto.SalivaSampleDataDTO;
import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record SampleDetailDTO(
        UUID id,

        String patientName,

        String registeredByName,

        @NotNull
        SampleType type,

        @NotNull
        SampleStatus status,

        LocalDate collectionDate,

        String notes,

        BloodSampleDataDTO bloodData,

        DnaSampleDataDTO dnaData,

        SalivaSampleDataDTO salivaData
) {}