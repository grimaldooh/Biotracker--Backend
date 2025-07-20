package com.biotrack.backend.services;

import com.biotrack.backend.models.MedicalVisit;

import java.util.List;
import java.util.UUID;

public interface MedicalVisitService {
    MedicalVisit create(MedicalVisit visit);
    MedicalVisit findById(UUID id);
    List<MedicalVisit> findByPatientId(UUID patientId);
    List<MedicalVisit> findAll();
    void deleteById(UUID id);
}