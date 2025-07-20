package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.MedicalVisit;
import com.biotrack.backend.repositories.MedicalVisitRepository;
import com.biotrack.backend.services.MedicalVisitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MedicalVisitServiceImpl implements MedicalVisitService {

    private final MedicalVisitRepository repository;

    public MedicalVisitServiceImpl(MedicalVisitRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public MedicalVisit create(MedicalVisit visit) {
        return repository.save(visit);
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
    public List<MedicalVisit> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }
}