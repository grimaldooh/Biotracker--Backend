package com.biotrack.backend.utils;

import com.biotrack.backend.dto.MedicalVisitCreationDTO;
import com.biotrack.backend.dto.MedicalVisitDTO;
import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.MedicalVisitType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        if (dto.getType() == null) {
            throw new IllegalArgumentException("Medical visit type is required and cannot be null");
        }
        visit.setType(MedicalVisitType.valueOf(dto.getType().name()));
        visit.setMedicalArea(dto.getMedicalArea());
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
                visit.getType(),
                visit.getMedicalArea()
        );
    }

    public static MedicalVisitDTO toBasicDTO(MedicalVisit visit) {
        return new MedicalVisitDTO(
            visit.getId(),
            visit.getPatient() != null ? visit.getPatient().getFirstName() + " " + visit.getPatient().getLastName() : null,
            visit.getDoctor() != null ? visit.getDoctor().getName() : null,
            visit.getVisitDate(),
            visit.getNotes(),
            visit.getDiagnosis(),
            visit.getRecommendations(),
            visit.getMedicalEntityId(),
            visit.isVisitCompleted(),
            visit.getType(),
            visit.getMedicalArea()
        );
    }

    public static List<MedicalVisitDTO> toDTOList(List<MedicalVisit> visits) {
        return visits.stream()
                .map(MedicalVisitMapper::toBasicDTO)
                .collect(Collectors.toList());
    }
}