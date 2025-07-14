package com.biotrack.backend.utils;

import com.biotrack.backend.dto.PatientDTO;
import com.biotrack.backend.models.Patient;

public class PatientMapper {

    public static PatientDTO toDTO(Patient patient){
        return new PatientDTO(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getBirtDate(),
                patient.getGender(),
                patient.getCurp()
        );
    }

    public static Patient toEntity(PatientDTO dto){
        return Patient.builder()
                .id(dto.id())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .birtDate(dto.birthDate())
                .gender(dto.gender())
                .curp(dto.curp())
                .build();
    }
}
