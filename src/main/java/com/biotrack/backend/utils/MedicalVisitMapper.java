package com.biotrack.backend.utils;

import com.biotrack.backend.dto.MedicalVisitCreationDTO;
import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.MedicalVisitType;

import java.time.LocalDateTime;

public class MedicalVisitMapper {

    public static MedicalVisit toEntity(
            MedicalVisitCreationDTO dto
    ) {
        MedicalVisit visit = new MedicalVisit();
        visit.setPatient(dto.getPatientId());
        visit.setDoctor(dto.getDoctorId());
        if (dto.getVisitDate() != null) {
            visit.setVisitDate(LocalDateTime.parse(dto.getVisitDate()));
        } else {
            visit.setVisitDate(LocalDateTime.now());
        }
        visit.setNotes(dto.getNotes());
        visit.setDiagnosis(dto.getDiagnosis());
        visit.setRecommendations(dto.getRecommendations());
        visit.setMedicalEntityId(dto.getMedicalEntityId());
        visit.setType(MedicalVisitType.valueOf(dto.getType().name()));
        return visit;
    }

    public static MedicalVisitCreationDTO toDTO(MedicalVisit visit) {
        return new MedicalVisitCreationDTO(
                visit.getPatient(),
                visit.getDoctor(),
                visit.getVisitDate().toString(),
                visit.getNotes(),
                visit.getDiagnosis(),
                visit.getRecommendations(),
                visit.getMedicalEntityId(),
                visit.getType()
        );
    }
}