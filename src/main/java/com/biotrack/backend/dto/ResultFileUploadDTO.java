package com.biotrack.backend.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ResultFileUploadDTO(
        @NotNull
        UUID geneticSampleId 
) {}