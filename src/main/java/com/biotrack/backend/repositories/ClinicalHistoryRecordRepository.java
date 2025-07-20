package com.biotrack.backend.repositories;

import com.biotrack.backend.models.ClinicalHistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClinicalHistoryRecordRepository extends JpaRepository<ClinicalHistoryRecord, UUID> {
    List<ClinicalHistoryRecord> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    ClinicalHistoryRecord findTopByPatientIdOrderByCreatedAtDesc(UUID patientId);
}