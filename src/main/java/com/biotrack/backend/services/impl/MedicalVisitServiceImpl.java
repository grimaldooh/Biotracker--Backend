package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.User;
import com.biotrack.backend.repositories.MedicalVisitRepository;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.repositories.UserRepository;
import com.biotrack.backend.services.MedicalVisitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MedicalVisitServiceImpl implements MedicalVisitService {

    private final MedicalVisitRepository repository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public MedicalVisitServiceImpl(MedicalVisitRepository repository, 
                                   PatientRepository patientRepository,
                                   UserRepository userRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
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
    public MedicalVisit submitAdvance(UUID id ,MedicalVisit visit) {
        MedicalVisit existingVisit = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical visit not found"));

        existingVisit.setNotes(visit.getNotes());
        existingVisit.setDiagnosis(visit.getDiagnosis());
        existingVisit.setRecommendations(visit.getRecommendations());
        existingVisit.setVisitCompleted(true);

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
}