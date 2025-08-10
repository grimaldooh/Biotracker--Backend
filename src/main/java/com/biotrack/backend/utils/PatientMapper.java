package com.biotrack.backend.utils;

import com.biotrack.backend.dto.PatientCreationDTO;
import com.biotrack.backend.dto.PatientDTO;
import com.biotrack.backend.models.Patient;

public class PatientMapper {

    public static PatientCreationDTO toDTOCreation(Patient patient){
        return new PatientCreationDTO(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getBirthDate(),
                patient.getGender(),
                patient.getEmail(),
                patient.getPassword(),
                patient.getPhoneNumber(),
                patient.getCurp()
        );
    }

    public static Patient toEntityCreation(PatientCreationDTO dto){
        return Patient.builder()
                .id(dto.id())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .birthDate(dto.birthDate())
                .gender(dto.gender())
                .email(dto.email())
                .password(dto.password())
                .phoneNumber(dto.phoneNumber())
                .curp(dto.curp())
                .build();
    }

    public static PatientDTO toDTO(Patient patient){
        return new PatientDTO(
                patient.getId(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getBirthDate(),
                patient.getGender(),
                patient.getEmail(),
                patient.getPhoneNumber(),
                patient.getCreatedAt().toString()
        );
    }

    public static Patient toEntity(PatientDTO dto){
        return Patient.builder()
                .id(dto.id())
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .birthDate(dto.birthDate())
                .gender(dto.gender())
                .email(dto.email())
                .phoneNumber(dto.phoneNumber())
                .build();
    }
}
