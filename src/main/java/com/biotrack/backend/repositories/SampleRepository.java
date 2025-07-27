package com.biotrack.backend.repositories;

import com.biotrack.backend.models.Sample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SampleRepository extends JpaRepository<Sample, UUID> {
    // Obtiene todos los samples vinculados a un paciente específico
    List<Sample> findByPatientId(UUID patientId);

    // Para varios pacientes
    List<Sample> findByPatientIdIn(List<UUID> patientIds);
}
