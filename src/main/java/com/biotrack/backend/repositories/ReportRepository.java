package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Report;
import com.biotrack.backend.models.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    
    @Query("SELECT r FROM Report r WHERE r.sample.id = :sampleId ORDER BY r.generatedAt DESC")
    List<Report> findBySampleIdOrderByGeneratedAtDesc(@Param("sampleId") UUID sampleId);
    
    @Query("SELECT r FROM Report r WHERE r.status = :status ORDER BY r.generatedAt DESC")
    List<Report> findByStatusOrderByGeneratedAtDesc(@Param("status") ReportStatus status);
    
    @Query("SELECT r FROM Report r WHERE r.sample.id = :sampleId AND r.status = 'COMPLETED' ORDER BY r.generatedAt DESC LIMIT 1")
    Optional<Report> findLatestCompletedBySampleId(@Param("sampleId") UUID sampleId);
    
    @Query("SELECT r FROM Report r WHERE r.sample.patient.id = :patientId ORDER BY r.generatedAt DESC")
    List<Report> findByPatientIdOrderByGeneratedAtDesc(UUID patientId);
    
    boolean existsBySampleIdAndStatus(UUID sampleId, ReportStatus status);

    void deleteAllBySampleId(UUID id);
}
