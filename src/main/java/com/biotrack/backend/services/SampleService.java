package com.biotrack.backend.services;

import com.biotrack.backend.models.Sample;

import java.util.List;
import java.util.UUID;

public interface SampleService {
    Sample create(Sample sample);
    List<Sample> findAll();
    Sample findById(UUID id);
    void deleteById(UUID id);
    Sample update(UUID id, Sample sample);
    List<Sample> findByPatientId(UUID patientId);
    List<Sample> findLatest10ByMedicalEntityId(UUID medicalEntityId);
}
