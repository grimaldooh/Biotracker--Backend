package com.biotrack.backend.repositories;

import com.biotrack.backend.models.LabAppointment;
import com.biotrack.backend.models.enums.LabAppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LabAppointmentRepository extends JpaRepository<LabAppointment, UUID> {
    List<LabAppointment> findByPatientId(UUID patientId);

    List<LabAppointment> findByMedicalEntityIdAndStatus(UUID medicalEntityId, LabAppointmentStatus status);
}