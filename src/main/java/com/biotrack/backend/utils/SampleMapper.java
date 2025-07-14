package com.biotrack.backend.utils;

import com.biotrack.backend.dto.SampleDTO;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.models.User;

public class SampleMapper {

    public static SampleDTO toDTO(Sample sample) {
        return new SampleDTO(
                sample.getId(),
                sample.getPatient().getId(),
                sample.getRegisteredBy().getId(),
                sample.getType(),
                sample.getStatus(),
                sample.getCollectionDate(),
                sample.getNotes()
        );
    }

    public static Sample toEntity(SampleDTO dto, Patient patient, User registeredBy) {
        return Sample.builder()
                .id(dto.id())
                .patient(patient)
                .registeredBy(registeredBy)
                .type(dto.type())
                .status(dto.status())
                .collectionDate(dto.collectionDate())
                .notes(dto.notes())
                .build();
    }
}