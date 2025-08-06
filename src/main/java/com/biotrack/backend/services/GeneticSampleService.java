package com.biotrack.backend.services;

import com.biotrack.backend.models.GeneticSample;

import java.util.List;
import java.util.UUID;

public interface GeneticSampleService {
    GeneticSample create(GeneticSample geneticSample);
    List<GeneticSample> findAll();
    GeneticSample findById(UUID id);
    void deleteById(UUID id);
    GeneticSample update(UUID id, GeneticSample geneticSample);
    List<GeneticSample> findByPatientId(UUID patientId);
    List<GeneticSample> findLatest10ByMedicalEntityId(UUID medicalEntityId);
    List<GeneticSample> findByMedicalEntityId(UUID medicalEntityId);
}