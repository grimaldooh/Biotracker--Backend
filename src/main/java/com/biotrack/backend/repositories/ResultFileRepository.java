package com.biotrack.backend.repositories;

import com.biotrack.backend.models.ResultFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ResultFileRepository extends JpaRepository<ResultFile, UUID> {
    
    @Query("SELECT rf FROM ResultFile rf WHERE rf.geneticSample.id = :geneticSampleId")
    List<ResultFile> findByGeneticSampleId(@Param("geneticSampleId") UUID geneticSampleId);
    
    @Query("SELECT rf FROM ResultFile rf WHERE rf.processingStatus = :status")
    List<ResultFile> findByProcessingStatus(@Param("status") String status);
    
    @Query("SELECT rf FROM ResultFile rf WHERE rf.geneticSample.id = :geneticSampleId AND rf.processingStatus = 'PENDING'")
    List<ResultFile> findPendingByGeneticSampleId(@Param("geneticSampleId") UUID geneticSampleId);
}