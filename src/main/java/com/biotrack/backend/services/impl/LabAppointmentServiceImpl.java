package com.biotrack.backend.services.impl;

import com.biotrack.backend.dto.LabAppointmentCreationDTO;
import com.biotrack.backend.dto.LabAppointmentDTO;
import com.biotrack.backend.models.LabAppointment;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;
import com.biotrack.backend.models.enums.LabAppointmentStatus;
import com.biotrack.backend.repositories.LabAppointmentRepository;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.LabAppointmentService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LabAppointmentServiceImpl implements LabAppointmentService {

    private final LabAppointmentRepository repository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public LabAppointmentServiceImpl(LabAppointmentRepository repository, UserRepository userRepository, PatientRepository patientRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
    }

    @Override
    public LabAppointmentDTO create(LabAppointmentCreationDTO dto) {
        User doctor = userRepository.findById(dto.doctorId()).orElseThrow(() -> new RuntimeException("Doctor not found"));
        Patient patient = patientRepository.findById(dto.patientId()).orElseThrow(() -> new RuntimeException("Patient not found"));

        LabAppointment appointment = LabAppointment.builder()
                .doctor(doctor)
                .patient(patient)
                .createdAt(LocalDateTime.now())
                .status(LabAppointmentStatus.SOLICITADA)
                .sampleType(dto.sampleType())
                .notes(dto.notes())
                .build();

        appointment = repository.save(appointment);

        return new LabAppointmentDTO(
                appointment.getId(),
                appointment.getMedicalEntityId(),
                doctor.getId(),
                patient.getId(),
                appointment.getCreatedAt(),
                appointment.getStatus(),
                appointment.getSampleType(),
                appointment.getNotes()
        );
    }

    @Override
    public List<LabAppointmentDTO> findByPatientId(UUID patientId) {
        return repository.findByPatientId(patientId).stream()
                .map(a -> new LabAppointmentDTO(
                        a.getId(),
                        a.getMedicalEntityId(),
                        a.getDoctor().getId(),
                        a.getPatient().getId(),
                        a.getCreatedAt(),
                        a.getStatus(),
                        a.getSampleType(),
                        a.getNotes()
                )).toList();
    }

    @Override
    public LabAppointmentDTO updateStatus(UUID appointmentId, String status) {
        LabAppointment appointment = repository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Lab appointment not found"));
        appointment.setStatus(LabAppointmentStatus.valueOf(status));
        appointment = repository.save(appointment);

        return new LabAppointmentDTO(
                appointment.getId(),
                appointment.getMedicalEntityId(),
                appointment.getDoctor().getId(),
                appointment.getPatient().getId(),
                appointment.getCreatedAt(),
                appointment.getStatus(),
                appointment.getSampleType(),
                appointment.getNotes()
        );
    }

    @Override
    public List<LabAppointmentDTO> findSolicitedByMedicalEntityId(UUID medicalEntityId) {
        return repository.findByMedicalEntityIdAndStatus(medicalEntityId, LabAppointmentStatus.SOLICITADA)
                .stream()
                .map(a -> new LabAppointmentDTO(
                        a.getId(),
                        a.getMedicalEntityId(),
                        a.getDoctor().getId(),
                        a.getPatient().getId(),
                        a.getCreatedAt(),
                        a.getStatus(),
                        a.getSampleType(),
                        a.getNotes()
                )).toList();
    }
}