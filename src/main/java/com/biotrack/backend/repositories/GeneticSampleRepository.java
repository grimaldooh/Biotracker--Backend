package com.biotrack.backend.repositories;

import com.biotrack.backend.models.GeneticSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GeneticSampleRepository extends JpaRepository<GeneticSample, UUID> {
    
    @Query("SELECT gs FROM GeneticSample gs WHERE gs.patient.id = :patientId")
    List<GeneticSample> findByPatientId(@Param("patientId") UUID patientId);

    @Query("SELECT gs FROM GeneticSample gs WHERE gs.patient.id IN :patientIds")
    List<GeneticSample> findByPatientIdIn(@Param("patientIds") List<UUID> patientIds);

    @Query("SELECT gs FROM GeneticSample gs WHERE gs.medicalEntityId = :medicalEntityId ORDER BY gs.collectionDate DESC")
    List<GeneticSample> findTop10ByMedicalEntityIdOrderByCollectionDateDesc(@Param("medicalEntityId") UUID medicalEntityId);

    @Query("SELECT gs FROM GeneticSample gs WHERE gs.medicalEntityId = :medicalEntityId")
    List<GeneticSample> findByMedicalEntityId(@Param("medicalEntityId") UUID medicalEntityId);
}