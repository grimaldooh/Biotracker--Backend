package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SampleRepository extends JpaRepository<Sample, UUID> {
    
    // ✅ este funciona bien
    @Query("SELECT s FROM Sample s WHERE s.patient.id = :patientId")
    List<Sample> findByPatientId(@Param("patientId") UUID patientId);

    // ✅ ahora también con JPQL, sin nativeQuery
    @Query("SELECT s FROM Sample s WHERE s.patient.id IN :patientIds")
    List<Sample> findByPatientIdIn(@Param("patientIds") List<UUID> patientIds);

    List<Sample> findTop10ByMedicalEntityIdOrderByCollectionDateDesc(@Param("medicalEntityId") UUID medicalEntityId);

    // Los métodos futuros que devuelvan entidades con herencia JOINED, usa siempre JPQL.
}
