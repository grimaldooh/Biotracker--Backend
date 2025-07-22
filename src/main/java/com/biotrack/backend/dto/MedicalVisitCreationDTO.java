package com.biotrack.backend.dto;

import java.util.UUID;

import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.MedicalVisitType;
import com.fasterxml.jackson.annotation.JsonInclude;

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
public class MedicalVisitCreationDTO{
    Patient patientId;
    User doctorId;
    String visitDate;
    String notes;
    String diagnosis;
    String recommendations;
    UUID medicalEntityId;
    MedicalVisitType type;
}