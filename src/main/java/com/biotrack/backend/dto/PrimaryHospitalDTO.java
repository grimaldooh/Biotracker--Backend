package com.biotrack.backend.dto;

import java.util.UUID;

public record PrimaryHospitalDTO(
    UUID id,
    String name,
    String fullAddress
) {
    public static PrimaryHospitalDTO fromHospital(com.biotrack.backend.models.Hospital hospital) {
        return new PrimaryHospitalDTO(
            hospital.getId(),
            hospital.getName(),
            hospital.getFullAddress()
        );
    }
}