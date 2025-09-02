package com.biotrack.backend.services.impl;

import com.biotrack.backend.dto.DoctorStatsDTO;
import com.biotrack.backend.dto.MedicalVisitDTO;
import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;
import com.biotrack.backend.repositories.MedicalVisitRepository;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.MedicalVisitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class MedicalVisitServiceImpl implements MedicalVisitService {

    private final MedicalVisitRepository repository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final PatientServiceImpl patientService;

    public MedicalVisitServiceImpl(MedicalVisitRepository repository,
                                   PatientRepository patientRepository,
                                   PatientServiceImpl patientService,
                                   UserRepository userRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.patientService = patientService;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public MedicalVisit create(MedicalVisit visit, UUID medical_entity_id) {

        Patient patient = patientRepository.findById(visit.getPatient().getId())
                .orElseThrow(() -> new RuntimeException("Patient not found"));
        User doctor = userRepository.findById(visit.getDoctor().getId())
                .orElseThrow(() -> new RuntimeException("Doctor not found"));

        visit.setMedicalEntityId(medical_entity_id);
        return repository.save(visit);
    }

    @Override
    @Transactional
    public MedicalVisit submitAdvance(UUID id, MedicalVisit visit) {
        MedicalVisit existingVisit = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical visit not found"));

        existingVisit.setNotes(visit.getNotes());
        existingVisit.setDiagnosis(visit.getDiagnosis());
        existingVisit.setRecommendations(visit.getRecommendations());
        existingVisit.setVisitCompleted(true);

        Patient patient = existingVisit.getPatient();
        if (patient == null || patient.getId() == null) {
            throw new RuntimeException("Patient not found");
        }

        // Busca todas las visitas completadas del paciente
        List<MedicalVisit> visits = repository.findByPatientIdAndVisitCompletedTrue(patient.getId());
        // Si la regla es "al menos 2", usa >= 2
        if (visits.size() >= 2) {
            patientService.generatePatientClinicalSummary(patient.getId());
        }

        return repository.save(existingVisit);
    }

    @Override
    public MedicalVisit findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical visit not found"));
    }

    @Override
    public List<MedicalVisit> findByPatientId(UUID patientId) {
        return repository.findByPatientId(patientId);
    }

    @Override
    public List<MedicalVisit> findPendingByPatientId(UUID patientId) {
        return repository.findByPatientIdAndVisitCompletedFalse(patientId);
    }

    @Override
    public List<MedicalVisit> findByDoctorId(UUID doctorId) {

        return repository.findByDoctorId(doctorId);
    }

    @Override
    public List<MedicalVisit> findPendingByDoctorId(UUID doctorId) {
        return repository.findByDoctorIdAndVisitCompletedFalse(doctorId);
    }

    @Override
    public List<MedicalVisit> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<MedicalVisit> findByMedicalEntityId(UUID medicalEntityId) {
        return repository.findByMedicalEntityId(medicalEntityId);
    }

    @Override
    public List<MedicalVisitDTO> addPatientVisitCounts(List<MedicalVisitDTO> visits) {
        return visits.stream()
                .map(visit -> {
                    int visitsCount = patientService.medicalVisitsCount(UUID.fromString(visit.patientId()));
                    return new MedicalVisitDTO(
                        visit.id(),
                        visit.patientName(),
                        visit.patientId(),
                        visit.doctorName(),
                        visit.visitDate(),
                        visit.notes(),
                        visit.diagnosis(),
                        visit.recommendations(),
                        visit.medicalEntityId(),
                        visit.visitCompleted(),
                        visit.type(),
                        visit.medicalArea(),
                        visitsCount // patientVisitsCount
                    );
                })
                .toList();
    }


    @Override
    public DoctorStatsDTO getDoctorStats(UUID doctorId) {
        // Todas las visitas del doctor
        List<MedicalVisit> allVisits = repository.findByDoctorId(doctorId);

        // Pacientes únicos
        Set<UUID> uniquePatients = new HashSet<>();
        for (MedicalVisit visit : allVisits) {
            if (visit.getPatient() != null) {
                uniquePatients.add(visit.getPatient().getId());
            }
        }
        int totalPatients = uniquePatients.size();

        // Hoy
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        int todayAppointments = (int) allVisits.stream()
                .filter(v -> v.getVisitDate() != null && !v.getVisitDate().isBefore(startOfDay) && !v.getVisitDate().isAfter(endOfDay))
                .count();

        // Próximas (futuras, no completadas)
        int upcomingAppointments = (int) allVisits.stream()
                .filter(v -> v.getVisitDate() != null && v.getVisitDate().isAfter(endOfDay) && !v.isVisitCompleted())
                .count();

        // Completadas
        int completedAppointments = (int) allVisits.stream()
                .filter(MedicalVisit::isVisitCompleted)
                .count();

        return new DoctorStatsDTO(totalPatients, todayAppointments, upcomingAppointments, completedAppointments);
    }
}