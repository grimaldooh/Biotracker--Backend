package com.biotrack.backend.services;

import com.biotrack.backend.dto.LabAppointmentCreationDTO;
import com.biotrack.backend.dto.LabAppointmentDTO;

import java.util.List;
import java.util.UUID;

public interface LabAppointmentService {
    LabAppointmentDTO create(LabAppointmentCreationDTO dto);
    List<LabAppointmentDTO> findByPatientId(UUID patientId);
    LabAppointmentDTO updateStatus(UUID appointmentId, String status);
    List<LabAppointmentDTO> findSolicitedByMedicalEntityId(UUID medicalEntityId);
}