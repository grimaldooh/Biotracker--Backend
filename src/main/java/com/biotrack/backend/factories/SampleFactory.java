package com.biotrack.backend.factories;

import com.biotrack.backend.dto.SampleCreationDTO;
import com.biotrack.backend.models.*;
import com.biotrack.backend.models.enums.DnaExtractionMethod;
import com.biotrack.backend.models.enums.SalivaCollectionMethod;
import com.biotrack.backend.models.enums.SequencingPlatform;
import com.biotrack.backend.dto.BloodSampleDataDTO;
import com.biotrack.backend.dto.DnaSampleDataDTO;
import com.biotrack.backend.dto.SalivaSampleDataDTO;

public class SampleFactory {
    public static Sample create(
        SampleCreationDTO dto,
        Patient patient,
        User registeredBy
    ) {
        switch (dto.type()) {
            case BLOOD: {
                BloodSampleDataDTO data = dto.bloodData();
                return BloodSample.builder()
                    .patient(patient)
                    .registeredBy(registeredBy)
                    .status(dto.status())
                    .collectionDate(dto.collectionDate())
                    .notes(dto.notes())
                    .createdAt(java.time.LocalDate.now())
                    .glucoseMgDl(data.getGlucoseMgDl())
                    .cholesterolTotalMgDl(data.getCholesterolTotalMgDl())
                    .cholesterolHdlMgDl(data.getCholesterolHdlMgDl())
                    .cholesterolLdlMgDl(data.getCholesterolLdlMgDl())
                    .triglyceridesMgDl(data.getTriglyceridesMgDl())
                    .creatinineMgDl(data.getCreatinineMgDl())
                    .ureaMgDl(data.getUreaMgDl())
                    .hemoglobinGDl(data.getHemoglobinGDl())
                    .hematocritPercent(data.getHematocritPercent())
                    .redBloodCellsMillionUl(data.getRedBloodCellsMillionUl())
                    .whiteBloodCellsThousandUl(data.getWhiteBloodCellsThousandUl())
                    .plateletsThousandUl(data.getPlateletsThousandUl())
                    .altSgptUL(data.getAltSgptUL())
                    .astSgotUL(data.getAstSgotUL())
                    .bilirubinTotalMgDl(data.getBilirubinTotalMgDl())
                    .alkalinePhosphataseUL(data.getAlkalinePhosphataseUL())
                    .bunMgDl(data.getBunMgDl())
                    .gfrMlMin(data.getGfrMlMin())
                    .totalProteinGDl(data.getTotalProteinGDl())
                    .albuminGDl(data.getAlbuminGDl())
                    .sodiumMeqL(data.getSodiumMeqL())
                    .potassiumMeqL(data.getPotassiumMeqL())
                    .chlorideMeqL(data.getChlorideMeqL())
                    .cReactiveProteinMgL(data.getCReactiveProteinMgL())
                    .esrMmHr(data.getEsrMmHr())
                    .geneticMarkersDetected(data.getGeneticMarkersDetected())
                    .geneticQualityScore(data.getGeneticQualityScore())
                    .labReferenceValues(data.getLabReferenceValues())
                    .analyzerModel(data.getAnalyzerModel())
                    .centrifugationSpeedRpm(data.getCentrifugationSpeedRpm())
                    .storageTemperatureCelsius(data.getStorageTemperatureCelsius())
                    .build();
            }
            case DNA: {
                DnaSampleDataDTO data = dto.dnaData();
                return DnaSample.builder()
                    .patient(patient)
                    .registeredBy(registeredBy)
                    .status(dto.status())
                    .collectionDate(dto.collectionDate())
                    .notes(dto.notes())
                    .createdAt(java.time.LocalDate.now())
                    .concentrationNgUl(data.getConcentrationNgUl())
                    .purity260280Ratio(data.getPurity260280Ratio())
                    .purity260230Ratio(data.getPurity260230Ratio())
                    .integrityNumber(data.getIntegrityNumber())
                    .extractionMethod(DnaExtractionMethod.fromDto(data.getExtractionMethod()))
                    .extractionDate(data.getExtractionDate())
                    .extractionTechnician(data.getExtractionTechnician())
                    .storageBuffer(data.getStorageBuffer())
                    .aliquotVolumeUl(data.getAliquotVolumeUl())
                    .freezerLocation(data.getFreezerLocation())
                    .sequencingPlatform(SequencingPlatform.fromDto(data.getSequencingPlatform()))
                    .sequencingDepth(data.getSequencingDepth())
                    .libraryPrepProtocol(data.getLibraryPrepProtocol())
                    .totalReads(data.getTotalReads())
                    .mappedReads(data.getMappedReads())
                    .mappingQualityScore(data.getMappingQualityScore())
                    .variantsDetected(data.getVariantsDetected())
                    .snpsDetected(data.getSnpsDetected())
                    .indelsDetected(data.getIndelsDetected())
                    .fastqR1Url(data.getFastqR1Url())
                    .fastqR2Url(data.getFastqR2Url())
                    .vcfFileUrl(data.getVcfFileUrl())
                    .bamFileUrl(data.getBamFileUrl())
                    .build();
            }
            case SALIVA: {
                SalivaSampleDataDTO data = dto.salivaData();
                return SalivaSample.builder()
                    .patient(patient)
                    .registeredBy(registeredBy)
                    .status(dto.status())
                    .collectionDate(dto.collectionDate())
                    .notes(dto.notes())
                    .createdAt(java.time.LocalDate.now())
                    .volumeMl(data.getVolumeMl())
                    .phLevel(data.getPhLevel())
                    .viscosity(data.getViscosity())
                    .dnaYieldNg(data.getDnaYieldNg())
                    .cellCountPerMl(data.getCellCountPerMl())
                    .collectionMethod(SalivaCollectionMethod.fromDto(data.getCollectionMethod()))
                    .fastingStatus(data.getFastingStatus())
                    .build();
            }
            default:
                throw new IllegalArgumentException("Tipo de muestra no soportado");
        }
    }
}
