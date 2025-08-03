package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Sample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SampleRepository extends JpaRepository<Sample, UUID> {
    // Obtiene todos los samples vinculados a un paciente específico
    List<Sample> findByPatientId(UUID patientId);

    // Para varios pacientes
    List<Sample> findByPatientIdIn(List<UUID> patientIds);

    // Obtiene los 10 samples más recientes de una entidad médica específica, ordenados por fecha de recolección
    @Query("SELECT s FROM Sample s WHERE s.medicalEntityId = :medicalEntityId ORDER BY s.collectionDate DESC")
    List<Sample> findTop10ByMedicalEntityIdOrderByCollectionDateDesc(@Param("medicalEntityId") UUID medicalEntityId);
}
