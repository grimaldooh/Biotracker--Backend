package com.biotrack.backend.dto;

import com.biotrack.backend.models.enums.Gender;
import com.biotrack.backend.models.enums.Role;

public record LoginResponseDTO(
    String email,
    String name,
    String firstName,
    String lastName,
    String phoneNumber,
    Gender gender,
    String specialty,
    Role role,
    String token // JWT token
) {
    // Constructor para Usuario
    public static LoginResponseDTO fromUser(com.biotrack.backend.models.User user, String token) {
        return new LoginResponseDTO(
            user.getEmail(),
            user.getName(),
            null, // firstName
            null, // lastName
            null, // phoneNumber
            null, // gender
            user.getSpecialty(),
            user.getRole(),
            token
        );
    }

    // Constructor para Paciente
    public static LoginResponseDTO fromPatient(com.biotrack.backend.models.Patient patient, String token) {
        return new LoginResponseDTO(
            patient.getEmail(),
            null, // name
            patient.getFirstName(),
            patient.getLastName(),
            patient.getPhoneNumber(),
            patient.getGender(),
            null, // specialty
            Role.PATIENT, // role fijo para pacientes
            token
        );
    }
}