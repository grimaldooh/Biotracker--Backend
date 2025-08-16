package com.biotrack.backend.repositories;

import com.biotrack.backend.models.MedicalVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface MedicalVisitRepository extends JpaRepository<MedicalVisit, UUID> {
    // Todas las citas de un paciente
    List<MedicalVisit> findByPatientId(UUID patientId);

    // Citas de un paciente que no han sido completadas
    List<MedicalVisit> findByPatientIdAndVisitCompletedFalse(UUID patientId);

    // Citas de un paciente que ya han sido completadas
    List<MedicalVisit> findByPatientIdAndVisitCompletedTrue(UUID patientId);

    // Todas las citas de un doctor (usuario)
    List<MedicalVisit> findByDoctorId(UUID doctorId);

    // Citas de un doctor que no han sido completadas
    List<MedicalVisit> findByDoctorIdAndVisitCompletedFalse(UUID doctorId);

    // Todas las citas de una entidad médica
    List<MedicalVisit> findByMedicalEntityId(UUID medicalEntityId);

    List<MedicalVisit> findByDoctorIdAndVisitDateBetween(UUID doctorId, LocalDateTime start, LocalDateTime end);

}