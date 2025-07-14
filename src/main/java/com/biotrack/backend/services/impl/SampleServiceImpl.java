package com.biotrack.backend.services.impl;

import com.biotrack.backend.repositories.SampleRepository;
import com.biotrack.backend.services.SampleService;
import com.biotrack.backend.models.Sample;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

public class SampleServiceImpl implements SampleService {

    private final SampleRepository sampleRepository;

    public SampleServiceImpl(SampleRepository sampleRepository){
        this.sampleRepository = sampleRepository;
    }

    @Override
    public Sample create(Sample sample){
        return sampleRepository.save(sample);
    }

    @Override
    public List<Sample> findAll() {
        return sampleRepository.findAll();
    }

    @Override
    public Sample findById(UUID id){
        return sampleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sample not found"));
    }

    @Override
    public void deleteById(UUID id){
        sampleRepository.deleteById(id);
    }

    @Override
    public Sample update(UUID id, Sample updatedSample) {
        Sample existing = findById(id);
        existing.setType(updatedSample.getType());
        existing.setStatus(updatedSample.getStatus());
        existing.setCollectionDate(updatedSample.getCollectionDate());
        existing.setNotes(updatedSample.getNotes());
        return sampleRepository.save(existing);
    }
}
