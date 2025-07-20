package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;
import com.biotrack.backend.models.enums.DnaExtractionMethod;
import com.biotrack.backend.models.enums.SequencingPlatform;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "dna_samples")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class DnaSample extends Sample {



    // === CALIDAD DEL DNA ===
    @Column(name = "concentration_ng_ul", precision = 8, scale = 2)
    private BigDecimal concentrationNgUl;

    @Column(name = "purity_260_280_ratio", precision = 4, scale = 2)
    private BigDecimal purity260280Ratio;

    @Column(name = "purity_260_230_ratio", precision = 4, scale = 2)
    private BigDecimal purity260230Ratio;

    @Column(name = "integrity_number", precision = 3, scale = 1)
    private BigDecimal integrityNumber;

    // === EXTRACCIÓN ===
    @Enumerated(EnumType.STRING)
    @Column(name = "extraction_method")
    private DnaExtractionMethod extractionMethod;

    @Column(name = "extraction_date")
    private LocalDateTime extractionDate;

    @Column(name = "extraction_technician")
    private String extractionTechnician;

    // ✅ AGREGAR ALMACENAMIENTO
    @Column(name = "storage_buffer")
    private String storageBuffer;

    @Column(name = "aliquot_volume_ul", precision = 6, scale = 1)
    private BigDecimal aliquotVolumeUl;

    @Column(name = "freezer_location")
    private String freezerLocation;

    // === SECUENCIACIÓN ===
    @Enumerated(EnumType.STRING)
    @Column(name = "sequencing_platform")
    private SequencingPlatform sequencingPlatform;

    @Column(name = "sequencing_depth")
    private Integer sequencingDepth;

    @Column(name = "library_prep_protocol")
    private String libraryPrepProtocol;

    // === ANÁLISIS GENÓMICO ===
    @Column(name = "total_reads")
    private Long totalReads;

    @Column(name = "mapped_reads")
    private Long mappedReads;

    // ✅ AGREGAR CAMPO FALTANTE
    @Column(name = "mapping_quality_score", precision = 4, scale = 1)
    private BigDecimal mappingQualityScore;

    @Column(name = "variants_detected")
    private Integer variantsDetected;

    @Column(name = "snps_detected")
    private Integer snpsDetected;

    // ✅ AGREGAR CAMPO FALTANTE
    @Column(name = "indels_detected")
    private Integer indelsDetected;

    // === ARCHIVOS DE SECUENCIACIÓN (S3) ===
    @Column(name = "fastq_r1_url")
    private String fastqR1Url;

    @Column(name = "fastq_r2_url")
    private String fastqR2Url;

    @Column(name = "vcf_file_url")
    private String vcfFileUrl;

    // ✅ AGREGAR CAMPO FALTANTE
    @Column(name = "bam_file_url")
    private String bamFileUrl;

    // Métodos sobrescritos si necesitas lógica específica
    @Override
    public SampleType getSampleType() { return SampleType.DNA; }

    @Override
    public String getSampleTypeDescription() {
        return "Muestra de DNA para análisis genómico completo";
    }

    @Override
    public boolean isGeneticAnalysisRequired() { return true; }

    @Override
    public boolean isValidForProcessing() {
        return concentrationNgUl != null && concentrationNgUl.compareTo(new BigDecimal("10")) >= 0;
    }

    @Override
    public String getSpecificSampleInfo() {
        StringBuilder info = new StringBuilder();
        info.append("DNA Sample Analysis:\n");
        if (concentrationNgUl != null) {
            info.append("- DNA Concentration: ").append(concentrationNgUl).append(" ng/μL\n");
        }
        if (purity260280Ratio != null) {
            info.append("- Purity (260/280): ").append(purity260280Ratio).append("\n");
        }
        if (sequencingDepth != null) {
            info.append("- Sequencing Depth: ").append(sequencingDepth).append("x\n");
        }
        if (variantsDetected != null) {
            info.append("- Variants Detected: ").append(variantsDetected).append("\n");
        }
        return info.toString();
    }

    public boolean hasHighQualityDNA() {
        return purity260280Ratio != null && 
               purity260280Ratio.compareTo(new BigDecimal("1.8")) >= 0 &&
               purity260280Ratio.compareTo(new BigDecimal("2.0")) <= 0;
    }

    public boolean hasAdequateCoverage() {
        return sequencingDepth != null && sequencingDepth >= 30;
    }
}

