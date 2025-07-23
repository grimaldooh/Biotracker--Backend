package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MedicationRepository extends JpaRepository<Medication, UUID> {
    List<Medication> findByPatientId(UUID patientId);
    List<Medication> findByPrescribedById(UUID userId);
}