package com.biotrack.backend.services;

import com.biotrack.backend.models.MedicalVisit;

import java.util.List;
import java.util.UUID;

public interface MedicalVisitService {
    MedicalVisit create(MedicalVisit visit, UUID medical_entity_id);
    MedicalVisit submitAdvance(UUID id, MedicalVisit visit);
    MedicalVisit findById(UUID id);
    List<MedicalVisit> findByPatientId(UUID patientId);
    List<MedicalVisit> findPendingByPatientId(UUID patientId);
    List<MedicalVisit> findByDoctorId(UUID doctorId);
    List<MedicalVisit> findPendingByDoctorId(UUID doctorId);
    List<MedicalVisit> findAll();
    void deleteById(UUID id);
    List<MedicalVisit> findByMedicalEntityId(UUID medicalEntityId);
}