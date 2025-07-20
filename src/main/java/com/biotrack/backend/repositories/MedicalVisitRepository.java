package com.biotrack.backend.repositories;

import com.biotrack.backend.models.MedicalVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MedicalVisitRepository extends JpaRepository<MedicalVisit, UUID> {
    List<MedicalVisit> findByPatientId(UUID patientId);
}