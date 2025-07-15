package com.biotrack.backend.repositories;

import com.biotrack.backend.models.ResultFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ResultFileRepository extends JpaRepository<ResultFile, UUID> {
    
    @Query("SELECT rf FROM ResultFile rf WHERE rf.sample.id = :sampleId")
    List<ResultFile> findBySampleId(@Param("sampleId") UUID sampleId);
    
    @Query("SELECT rf FROM ResultFile rf WHERE rf.s3Key = :s3Key")
    ResultFile findByS3Key(@Param("s3Key") String s3Key);
}