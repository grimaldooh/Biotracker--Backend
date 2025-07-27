package com.biotrack.backend.utils;

import com.biotrack.backend.dto.BloodSampleDataDTO;
import com.biotrack.backend.dto.DnaExtractionMethodDTO;
import com.biotrack.backend.dto.DnaSampleDataDTO;
import com.biotrack.backend.dto.SalivaCollectionMethodDTO;
import com.biotrack.backend.dto.SalivaSampleDataDTO;
import com.biotrack.backend.dto.SampleDTO;
import com.biotrack.backend.dto.SequencingPlatformDTO;
import com.biotrack.backend.dto.Samples.SampleDetailDTO;
import com.biotrack.backend.models.BloodSample;
import com.biotrack.backend.models.DnaSample;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.SalivaSample;
import com.biotrack.backend.models.Sample;
import com.biotrack.backend.models.User;

public class SampleMapper {

    public static SampleDTO toDTO(Sample sample) {
        if (sample instanceof BloodSample blood) {
            return new SampleDTO(
                blood.getId(),
                blood.getPatient().getId(),
                blood.getRegisteredBy().getId(),
                blood.getType(),
                blood.getStatus(),
                blood.getCollectionDate(),
                blood.getNotes(),
                // Mapea los datos específicos
                new BloodSampleDataDTO(
                    blood.getGlucoseMgDl(),
                    blood.getCholesterolTotalMgDl(),
                    blood.getCholesterolHdlMgDl(),
                    blood.getCholesterolLdlMgDl(),
                    blood.getTriglyceridesMgDl(),
                    blood.getCreatinineMgDl(),
                    blood.getUreaMgDl(),
                    blood.getHemoglobinGDl(),
                    blood.getHematocritPercent(),
                    blood.getRedBloodCellsMillionUl(),
                    blood.getWhiteBloodCellsThousandUl(),
                    blood.getPlateletsThousandUl(),
                    blood.getAltSgptUL(),
                    blood.getAstSgotUL(),
                    blood.getBilirubinTotalMgDl(),
                    blood.getAlkalinePhosphataseUL(),
                    blood.getBunMgDl(),
                    blood.getGfrMlMin(),
                    blood.getTotalProteinGDl(),
                    blood.getAlbuminGDl(),
                    blood.getSodiumMeqL(),
                    blood.getPotassiumMeqL(),
                    blood.getChlorideMeqL(),
                    blood.getCReactiveProteinMgL(),
                    blood.getEsrMmHr(),
                    blood.getGeneticMarkersDetected(),
                    blood.getGeneticQualityScore(),
                    blood.getLabReferenceValues(),
                    blood.getAnalyzerModel(),
                    blood.getCentrifugationSpeedRpm(),
                    blood.getStorageTemperatureCelsius()
                ),
                null,
                null
            );
        }
        if (sample instanceof DnaSample dna) {
            return new SampleDTO(
                dna.getId(),
                dna.getPatient().getId(),
                dna.getRegisteredBy().getId(),
                dna.getType(),
                dna.getStatus(),
                dna.getCollectionDate(),
                dna.getNotes(),
                null,
                new DnaSampleDataDTO(
                    dna.getConcentrationNgUl(),
                    dna.getPurity260280Ratio(),
                    dna.getPurity260230Ratio(),
                    dna.getIntegrityNumber(),
                    dna.getExtractionMethod() != null ? DnaExtractionMethodDTO.valueOf(dna.getExtractionMethod().name()) : null,
                    dna.getExtractionDate(),
                    dna.getExtractionTechnician(),
                    dna.getStorageBuffer(),
                    dna.getAliquotVolumeUl(),
                    dna.getFreezerLocation(),
                    dna.getSequencingPlatform() != null ? SequencingPlatformDTO.valueOf(dna.getSequencingPlatform().name()) : null,
                    dna.getSequencingDepth(),
                    dna.getLibraryPrepProtocol(),
                    dna.getTotalReads(),
                    dna.getMappedReads(),
                    dna.getMappingQualityScore(),
                    dna.getVariantsDetected(),
                    dna.getSnpsDetected(),
                    dna.getIndelsDetected(),
                    dna.getFastqR1Url(),
                    dna.getFastqR2Url(),
                    dna.getVcfFileUrl(),
                    dna.getBamFileUrl()
                ),
                null
            );
        }
        if (sample instanceof SalivaSample saliva) {
            return new SampleDTO(
                saliva.getId(),
                saliva.getPatient().getId(),
                saliva.getRegisteredBy().getId(),
                saliva.getType(),
                saliva.getStatus(),
                saliva.getCollectionDate(),
                saliva.getNotes(),
                null,
                null,
                new SalivaSampleDataDTO(
                    saliva.getVolumeMl(),
                    saliva.getPhLevel(),
                    saliva.getViscosity(),
                    saliva.getDnaYieldNg(),
                    saliva.getCellCountPerMl(),
                    saliva.getCollectionMethod() != null ? SalivaCollectionMethodDTO.valueOf(saliva.getCollectionMethod().name()) : null,
                    saliva.getFastingStatus(),
                    saliva.getContaminationLevel(),
                    saliva.getPreservativeUsed(),
                    saliva.getTimeToProcessingHours()
                )
            );
        }
        // Si no es ninguno, regresa solo los datos comunes
        return new SampleDTO(
            sample.getId(),
            sample.getPatient().getId(),
            sample.getRegisteredBy().getId(),
            sample.getType(),
            sample.getStatus(),
            sample.getCollectionDate(),
            sample.getNotes(),
            null,
            null,
            null
        );
    }

    public static SampleDetailDTO toDetailDTO(Sample sample) {
        String patientName = sample.getPatient() != null ? sample.getPatient().getFirstName() + " " + sample.getPatient().getLastName() : null;
        String registeredByName = sample.getRegisteredBy() != null ? sample.getRegisteredBy().getName() : null;

        if (sample instanceof BloodSample blood) {
            return new SampleDetailDTO(
                blood.getId(),
                patientName,
                registeredByName,
                blood.getType(),
                blood.getStatus(),
                blood.getCollectionDate(),
                blood.getNotes(),
                new BloodSampleDataDTO(
                    blood.getGlucoseMgDl(),
                    blood.getCholesterolTotalMgDl(),
                    blood.getCholesterolHdlMgDl(),
                    blood.getCholesterolLdlMgDl(),
                    blood.getTriglyceridesMgDl(),
                    blood.getCreatinineMgDl(),
                    blood.getUreaMgDl(),
                    blood.getHemoglobinGDl(),
                    blood.getHematocritPercent(),
                    blood.getRedBloodCellsMillionUl(),
                    blood.getWhiteBloodCellsThousandUl(),
                    blood.getPlateletsThousandUl(),
                    blood.getAltSgptUL(),
                    blood.getAstSgotUL(),
                    blood.getBilirubinTotalMgDl(),
                    blood.getAlkalinePhosphataseUL(),
                    blood.getBunMgDl(),
                    blood.getGfrMlMin(),
                    blood.getTotalProteinGDl(),
                    blood.getAlbuminGDl(),
                    blood.getSodiumMeqL(),
                    blood.getPotassiumMeqL(),
                    blood.getChlorideMeqL(),
                    blood.getCReactiveProteinMgL(),
                    blood.getEsrMmHr(),
                    blood.getGeneticMarkersDetected(),
                    blood.getGeneticQualityScore(),
                    blood.getLabReferenceValues(),
                    blood.getAnalyzerModel(),
                    blood.getCentrifugationSpeedRpm(),
                    blood.getStorageTemperatureCelsius()
                ),
                null,
                null
            );
        }
        if (sample instanceof DnaSample dna) {
            return new SampleDetailDTO(
                dna.getId(),
                patientName,
                registeredByName,
                dna.getType(),
                dna.getStatus(),
                dna.getCollectionDate(),
                dna.getNotes(),
                null,
                new DnaSampleDataDTO(
                    dna.getConcentrationNgUl(),
                    dna.getPurity260280Ratio(),
                    dna.getPurity260230Ratio(),
                    dna.getIntegrityNumber(),
                    dna.getExtractionMethod() != null ? DnaExtractionMethodDTO.valueOf(dna.getExtractionMethod().name()) : null,
                    dna.getExtractionDate(),
                    dna.getExtractionTechnician(),
                    dna.getStorageBuffer(),
                    dna.getAliquotVolumeUl(),
                    dna.getFreezerLocation(),
                    dna.getSequencingPlatform() != null ? SequencingPlatformDTO.valueOf(dna.getSequencingPlatform().name()) : null,
                    dna.getSequencingDepth(),
                    dna.getLibraryPrepProtocol(),
                    dna.getTotalReads(),
                    dna.getMappedReads(),
                    dna.getMappingQualityScore(),
                    dna.getVariantsDetected(),
                    dna.getSnpsDetected(),
                    dna.getIndelsDetected(),
                    dna.getFastqR1Url(),
                    dna.getFastqR2Url(),
                    dna.getVcfFileUrl(),
                    dna.getBamFileUrl()
                ),
                null
            );
        }
        if (sample instanceof SalivaSample saliva) {
            return new SampleDetailDTO(
                saliva.getId(),
                patientName,
                registeredByName,
                saliva.getType(),
                saliva.getStatus(),
                saliva.getCollectionDate(),
                saliva.getNotes(),
                null,
                null,
                new SalivaSampleDataDTO(
                    saliva.getVolumeMl(),
                    saliva.getPhLevel(),
                    saliva.getViscosity(),
                    saliva.getDnaYieldNg(),
                    saliva.getCellCountPerMl(),
                    saliva.getCollectionMethod() != null ? SalivaCollectionMethodDTO.valueOf(saliva.getCollectionMethod().name()) : null,
                    saliva.getFastingStatus(),
                    saliva.getContaminationLevel(),
                    saliva.getPreservativeUsed(),
                    saliva.getTimeToProcessingHours()
                )
            );
        }
        // Si no es ninguno, regresa solo los datos comunes
        return new SampleDetailDTO(
            sample.getId(),
            patientName,
            registeredByName,
            sample.getType(),
            sample.getStatus(),
            sample.getCollectionDate(),
            sample.getNotes(),
            null,
            null,
            null
        );
    }
}