package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.Medication;
import com.biotrack.backend.repositories.MedicationRepository;
import com.biotrack.backend.services.MedicationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MedicationServiceImpl implements MedicationService {

    private final MedicationRepository repository;

    public MedicationServiceImpl(MedicationRepository repository) {
        this.repository = repository;
    }

    @Override
    public Medication create(Medication medication) {
        return repository.save(medication);
    }

    @Override
    public List<Medication> findAll() {
        return repository.findAll();
    }

    @Override
    public Medication findById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medication not found"));
    }

    @Override
    public void deleteById(UUID id) {
        repository.deleteById(id);
    }

    @Override
    public List<Medication> findByPatientId(UUID patientId) {
        return repository.findByPatientId(patientId);
    }

    @Override
    public List<Medication> findByPrescribedById(UUID userId) {
        return repository.findByPrescribedById(userId);
    }
}