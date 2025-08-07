package com.biotrack.backend.services.impl;

import com.biotrack.backend.models.GeneticSample;
import com.biotrack.backend.repositories.GeneticSampleRepository;
import com.biotrack.backend.services.GeneticSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class GeneticSampleServiceImpl implements GeneticSampleService {

    private final GeneticSampleRepository geneticSampleRepository;

    @Autowired
    public GeneticSampleServiceImpl(GeneticSampleRepository geneticSampleRepository) {
        this.geneticSampleRepository = geneticSampleRepository;
    }

    @Override
    @Transactional
    public GeneticSample create(GeneticSample geneticSample) {
        geneticSample.setCreatedAt(LocalDate.now());
        
        // ✅ GUARDAR: La muestra genética (las mutaciones se guardan en cascada)
        GeneticSample saved = geneticSampleRepository.save(geneticSample);
        
        // ✅ ASEGURAR: Que las mutaciones apunten a la muestra guardada
        if (saved.getMutations() != null) {
            saved.getMutations().forEach(mutation -> {
                if (mutation.getSample() == null) {
                    mutation.setSample(saved);
                }
            });
        }
        
        return saved;
    }

    @Override
    public List<GeneticSample> findAll() {
        return geneticSampleRepository.findAll();
    }

    @Override
    public GeneticSample findById(UUID id) {
        return geneticSampleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("GeneticSample not found with id: " + id));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        if (!geneticSampleRepository.existsById(id)) {
            throw new RuntimeException("GeneticSample not found with id: " + id);
        }
        geneticSampleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public GeneticSample update(UUID id, GeneticSample updatedGeneticSample) {
        GeneticSample existing = findById(id);
        
        existing.setType(updatedGeneticSample.getType());
        existing.setStatus(updatedGeneticSample.getStatus());
        existing.setCollectionDate(updatedGeneticSample.getCollectionDate());
        existing.setNotes(updatedGeneticSample.getNotes());
        existing.setConfidenceScore(updatedGeneticSample.getConfidenceScore());
        existing.setProcessingSoftware(updatedGeneticSample.getProcessingSoftware());
        existing.setReferenceGenome(updatedGeneticSample.getReferenceGenome());
        
        return geneticSampleRepository.save(existing);
    }

    @Override
    public List<GeneticSample> findByPatientId(UUID patientId) {
        return geneticSampleRepository.findByPatientId(patientId);
    }

    @Override
    public List<GeneticSample> findLatest10ByMedicalEntityId(UUID medicalEntityId) {
        return geneticSampleRepository.findTop10ByMedicalEntityIdOrderByCollectionDateDesc(medicalEntityId);
    }

    @Override
    public List<GeneticSample> findByMedicalEntityId(UUID medicalEntityId) {
        return geneticSampleRepository.findByMedicalEntityId(medicalEntityId);
    }
}