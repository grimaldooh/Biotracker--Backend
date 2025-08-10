package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Report;
import com.biotrack.backend.models.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Query("""
        SELECT r FROM Report r
        WHERE r.sample.patient.id = :patientId
        AND r.sample IS NOT NULL
        AND (r.s3Url IS NOT NULL OR r.s3UrlPatient IS NOT NULL)
        ORDER BY r.generatedAt DESC
    """)
    List<Report> findReportsByPatientId(@Param("patientId") UUID patientId);
    
    @Query("""
        SELECT r FROM Report r
        WHERE r.sample.id = :sampleId
        AND (r.s3Url IS NOT NULL OR r.s3UrlPatient IS NOT NULL)
        ORDER BY r.generatedAt DESC
    """)
    List<Report> findReportsBySampleId(@Param("sampleId") UUID sampleId);
}
