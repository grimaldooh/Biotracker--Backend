package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.SampleType;
import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SalivaCollectionMethod;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "saliva_samples")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class SalivaSample extends Sample {

    @Column(name = "volume_ml", precision = 4, scale = 1)
    private BigDecimal volumeMl;

    @Column(name = "ph_level", precision = 3, scale = 1)
    private BigDecimal phLevel;

    @Column(name = "viscosity")
    private String viscosity;

    @Column(name = "dna_yield_ng", precision = 8, scale = 2)
    private BigDecimal dnaYieldNg;

    @Column(name = "cell_count_per_ml")
    private Integer cellCountPerMl;

    @Enumerated(EnumType.STRING)
    @Column(name = "collection_method")
    private SalivaCollectionMethod collectionMethod;

    @Column(name = "fasting_status")
    private Boolean fastingStatus;

    @Column(name = "contamination_level")
    private String contaminationLevel;

    @Column(name = "preservative_used")
    private String preservativeUsed;

    @Column(name = "time_to_processing_hours")
    private Integer timeToProcessingHours;

    // Métodos sobrescritos si necesitas lógica específica
    @Override
    public SampleType getSampleType() { return SampleType.SALIVA; }

    @Override
    public String getSampleTypeDescription() {
        return "Muestra de saliva para análisis genético no invasivo";
    }

    @Override
    public boolean isGeneticAnalysisRequired() {
        return true;
    }

    @Override
    public boolean isValidForProcessing() {
        return volumeMl != null && volumeMl.compareTo(new BigDecimal("2.0")) >= 0;
    }

    @Override
    public String getSpecificSampleInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Saliva Sample Analysis:\n");
        if (volumeMl != null) {
            info.append("- Volume: ").append(volumeMl).append(" mL\n");
        }
        if (dnaYieldNg != null) {
            info.append("- DNA Yield: ").append(dnaYieldNg).append(" ng\n");
        }
        if (collectionMethod != null) {
            info.append("- Collection Method: ").append(collectionMethod).append("\n");
        }
        return info.toString();
    }
}