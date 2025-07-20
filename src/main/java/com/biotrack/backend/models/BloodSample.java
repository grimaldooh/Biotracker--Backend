package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.SampleStatus;
import com.biotrack.backend.models.enums.SampleType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "blood_samples")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class BloodSample extends Sample {

    // Solo los campos específicos de BloodSample
    @Column(name = "glucose_mg_dl", precision = 5, scale = 2)
    private BigDecimal glucoseMgDl;

    @Column(name = "cholesterol_total_mg_dl", precision = 5, scale = 2)
    private BigDecimal cholesterolTotalMgDl;

    @Column(name = "cholesterol_hdl_mg_dl", precision = 5, scale = 2)
    private BigDecimal cholesterolHdlMgDl;

    @Column(name = "cholesterol_ldl_mg_dl", precision = 5, scale = 2)
    private BigDecimal cholesterolLdlMgDl;

    @Column(name = "triglycerides_mg_dl", precision = 5, scale = 2)
    private BigDecimal triglyceridesMgDl;

    @Column(name = "creatinine_mg_dl", precision = 4, scale = 2)
    private BigDecimal creatinineMgDl;

    @Column(name = "urea_mg_dl", precision = 5, scale = 2)
    private BigDecimal ureaMgDl;

    @Column(name = "hemoglobin_g_dl", precision = 4, scale = 1)
    private BigDecimal hemoglobinGDl;

    @Column(name = "hematocrit_percent", precision = 4, scale = 1)
    private BigDecimal hematocritPercent;

    @Column(name = "red_blood_cells_million_ul", precision = 4, scale = 2)
    private BigDecimal redBloodCellsMillionUl;

    @Column(name = "white_blood_cells_thousand_ul", precision = 5, scale = 1)
    private BigDecimal whiteBloodCellsThousandUl;

    @Column(name = "platelets_thousand_ul", precision = 5, scale = 0)
    private BigDecimal plateletsThousandUl;

    @Column(name = "alt_sgpt_u_l", precision = 5, scale = 1)
    private BigDecimal altSgptUL;

    @Column(name = "ast_sgot_u_l", precision = 5, scale = 1)
    private BigDecimal astSgotUL;

    @Column(name = "bilirubin_total_mg_dl", precision = 4, scale = 2)
    private BigDecimal bilirubinTotalMgDl;

    @Column(name = "alkaline_phosphatase_u_l", precision = 5, scale = 1)
    private BigDecimal alkalinePhosphataseUL;

    @Column(name = "bun_mg_dl", precision = 4, scale = 1)
    private BigDecimal bunMgDl;

    @Column(name = "gfr_ml_min", precision = 5, scale = 1)
    private BigDecimal gfrMlMin;

    @Column(name = "total_protein_g_dl", precision = 4, scale = 1)
    private BigDecimal totalProteinGDl;

    @Column(name = "albumin_g_dl", precision = 4, scale = 1)
    private BigDecimal albuminGDl;

    @Column(name = "sodium_meq_l", precision = 5, scale = 1)
    private BigDecimal sodiumMeqL;

    @Column(name = "potassium_meq_l", precision = 4, scale = 1)
    private BigDecimal potassiumMeqL;

    @Column(name = "chloride_meq_l", precision = 5, scale = 1)
    private BigDecimal chlorideMeqL;

    @Column(name = "c_reactive_protein_mg_l", precision = 5, scale = 2)
    private BigDecimal cReactiveProteinMgL;

    @Column(name = "esr_mm_hr", precision = 3, scale = 0)
    private BigDecimal esrMmHr;

    @Column(name = "genetic_markers_detected")
    private Integer geneticMarkersDetected;

    @Column(name = "genetic_quality_score", precision = 4, scale = 2)
    private BigDecimal geneticQualityScore;

    @Column(name = "lab_reference_values", columnDefinition = "TEXT")
    private String labReferenceValues;

    @Column(name = "analyzer_model")
    private String analyzerModel;

    @Column(name = "centrifugation_speed_rpm")
    private Integer centrifugationSpeedRpm;

    @Column(name = "storage_temperature_celsius")
    private Integer storageTemperatureCelsius;

    // Métodos sobrescritos si necesitas lógica específica
    @Override
    public SampleType getSampleType() { return SampleType.BLOOD; }

    @Override
    public String getSampleTypeDescription() {
        return "Muestra de sangre completa con análisis bioquímico y hematológico";
    }

    @Override
    public boolean isGeneticAnalysisRequired() { return true; }

    @Override
    public boolean isValidForProcessing() {
        return hemoglobinGDl != null && hemoglobinGDl.compareTo(BigDecimal.ZERO) > 0;
    }

    @Override
    public String getSpecificSampleInfo() {
        StringBuilder info = new StringBuilder();
        info.append("Blood Sample Analysis:\n");
        if (glucoseMgDl != null) {
            info.append("- Glucose: ").append(glucoseMgDl).append(" mg/dL\n");
        }
        if (hemoglobinGDl != null) {
            info.append("- Hemoglobin: ").append(hemoglobinGDl).append(" g/dL\n");
        }
        if (cholesterolTotalMgDl != null) {
            info.append("- Total Cholesterol: ").append(cholesterolTotalMgDl).append(" mg/dL\n");
        }
        return info.toString();
    }

    // Métodos de utilidad para validaciones clínicas
    public boolean hasAbnormalGlucose() {
        return glucoseMgDl != null && (glucoseMgDl.compareTo(new BigDecimal("70")) < 0 
               || glucoseMgDl.compareTo(new BigDecimal("140")) > 0);
    }

    public boolean hasHighCholesterol() {
        return cholesterolTotalMgDl != null && cholesterolTotalMgDl.compareTo(new BigDecimal("200")) > 0;
    }

    public boolean isAnemiaIndicated() {
        return hemoglobinGDl != null && hemoglobinGDl.compareTo(new BigDecimal("12.0")) < 0;
    }
}