package com.biotrack.backend.services.impl;

import com.biotrack.backend.repositories.SampleRepository;
import com.biotrack.backend.services.SampleService;
import com.biotrack.backend.models.BloodSample;
import com.biotrack.backend.models.DnaSample;
import com.biotrack.backend.models.SalivaSample;
import com.biotrack.backend.models.Sample;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
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

        if (existing instanceof BloodSample && updatedSample instanceof BloodSample) {
            BloodSample blood = (BloodSample) existing;
            BloodSample updated = (BloodSample) updatedSample;
            blood.setStatus(updated.getStatus());
            blood.setCollectionDate(updated.getCollectionDate());
            blood.setNotes(updated.getNotes());
            blood.setCreatedAt(updated.getCreatedAt());
            blood.setGlucoseMgDl(updated.getGlucoseMgDl());
            blood.setCholesterolTotalMgDl(updated.getCholesterolTotalMgDl());
            blood.setCholesterolHdlMgDl(updated.getCholesterolHdlMgDl());
            blood.setCholesterolLdlMgDl(updated.getCholesterolLdlMgDl());
            blood.setTriglyceridesMgDl(updated.getTriglyceridesMgDl());
            blood.setCreatinineMgDl(updated.getCreatinineMgDl());
            blood.setUreaMgDl(updated.getUreaMgDl());
            blood.setHemoglobinGDl(updated.getHemoglobinGDl());
            blood.setHematocritPercent(updated.getHematocritPercent());
            blood.setRedBloodCellsMillionUl(updated.getRedBloodCellsMillionUl());
            blood.setWhiteBloodCellsThousandUl(updated.getWhiteBloodCellsThousandUl());
            blood.setPlateletsThousandUl(updated.getPlateletsThousandUl());
            blood.setAltSgptUL(updated.getAltSgptUL());
            blood.setAstSgotUL(updated.getAstSgotUL());
            blood.setBilirubinTotalMgDl(updated.getBilirubinTotalMgDl());
            blood.setAlkalinePhosphataseUL(updated.getAlkalinePhosphataseUL());
            blood.setBunMgDl(updated.getBunMgDl());
            blood.setGfrMlMin(updated.getGfrMlMin());
            blood.setTotalProteinGDl(updated.getTotalProteinGDl());
            blood.setAlbuminGDl(updated.getAlbuminGDl());
            blood.setSodiumMeqL(updated.getSodiumMeqL());
            blood.setPotassiumMeqL(updated.getPotassiumMeqL());
            blood.setChlorideMeqL(updated.getChlorideMeqL());
            blood.setCReactiveProteinMgL(updated.getCReactiveProteinMgL());
            blood.setEsrMmHr(updated.getEsrMmHr());
            blood.setGeneticMarkersDetected(updated.getGeneticMarkersDetected());
            blood.setGeneticQualityScore(updated.getGeneticQualityScore());
            blood.setLabReferenceValues(updated.getLabReferenceValues());
            blood.setAnalyzerModel(updated.getAnalyzerModel());
            blood.setCentrifugationSpeedRpm(updated.getCentrifugationSpeedRpm());
            blood.setStorageTemperatureCelsius(updated.getStorageTemperatureCelsius());
            return sampleRepository.save(blood);
        }
        if (existing instanceof DnaSample && updatedSample instanceof DnaSample) {
            DnaSample dna = (DnaSample) existing;
            DnaSample updated = (DnaSample) updatedSample;
            dna.setStatus(updated.getStatus());
            dna.setCollectionDate(updated.getCollectionDate());
            dna.setNotes(updated.getNotes());
            dna.setCreatedAt(updated.getCreatedAt());
            dna.setConcentrationNgUl(updated.getConcentrationNgUl());
            dna.setPurity260280Ratio(updated.getPurity260280Ratio());
            dna.setPurity260230Ratio(updated.getPurity260230Ratio());
            dna.setIntegrityNumber(updated.getIntegrityNumber());
            dna.setExtractionMethod(updated.getExtractionMethod());
            dna.setExtractionDate(updated.getExtractionDate());
            dna.setExtractionTechnician(updated.getExtractionTechnician());
            dna.setStorageBuffer(updated.getStorageBuffer());
            dna.setAliquotVolumeUl(updated.getAliquotVolumeUl());
            dna.setFreezerLocation(updated.getFreezerLocation());
            dna.setSequencingPlatform(updated.getSequencingPlatform());
            dna.setSequencingDepth(updated.getSequencingDepth());
            dna.setLibraryPrepProtocol(updated.getLibraryPrepProtocol());
            dna.setTotalReads(updated.getTotalReads());
            dna.setMappedReads(updated.getMappedReads());
            dna.setMappingQualityScore(updated.getMappingQualityScore());
            dna.setVariantsDetected(updated.getVariantsDetected());
            dna.setSnpsDetected(updated.getSnpsDetected());
            dna.setIndelsDetected(updated.getIndelsDetected());
            dna.setFastqR1Url(updated.getFastqR1Url());
            dna.setFastqR2Url(updated.getFastqR2Url());
            dna.setVcfFileUrl(updated.getVcfFileUrl());
            dna.setBamFileUrl(updated.getBamFileUrl());
            return sampleRepository.save(dna);
        }
        if (existing instanceof SalivaSample && updatedSample instanceof SalivaSample) {
            SalivaSample saliva = (SalivaSample) existing;
            SalivaSample updated = (SalivaSample) updatedSample;
            saliva.setStatus(updated.getStatus());
            saliva.setCollectionDate(updated.getCollectionDate());
            saliva.setNotes(updated.getNotes());
            saliva.setCreatedAt(updated.getCreatedAt());
            saliva.setVolumeMl(updated.getVolumeMl());
            saliva.setPhLevel(updated.getPhLevel());
            saliva.setViscosity(updated.getViscosity());
            saliva.setDnaYieldNg(updated.getDnaYieldNg());
            saliva.setCellCountPerMl(updated.getCellCountPerMl());
            saliva.setCollectionMethod(updated.getCollectionMethod());
            saliva.setFastingStatus(updated.getFastingStatus());
            return sampleRepository.save(saliva);
        }
        throw new IllegalArgumentException("Tipo de muestra no soportado");
    }
}
