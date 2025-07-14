package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;


public record PatientDTO(
        UUID id,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotNull LocalDate birthDate,
        @NotNull Gender gender,
        String curp
) {}
