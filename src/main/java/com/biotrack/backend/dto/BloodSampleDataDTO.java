package com.biotrack.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BloodSampleDataDTO {
    
    // === QUÍMICA SANGUÍNEA ===
    @DecimalMin(value = "0.0", message = "Glucose level cannot be negative")
    private BigDecimal glucoseMgDl;
    
    @DecimalMin(value = "0.0", message = "Cholesterol level cannot be negative")
    private BigDecimal cholesterolTotalMgDl;
    
    @DecimalMin(value = "0.0", message = "HDL cholesterol level cannot be negative")
    private BigDecimal cholesterolHdlMgDl;
    
    @DecimalMin(value = "0.0", message = "LDL cholesterol level cannot be negative")
    private BigDecimal cholesterolLdlMgDl;
    
    @DecimalMin(value = "0.0", message = "Triglycerides level cannot be negative")
    private BigDecimal triglyceridesMgDl;
    
    @DecimalMin(value = "0.0", message = "Creatinine level cannot be negative")
    private BigDecimal creatinineMgDl;
    
    @DecimalMin(value = "0.0", message = "Urea level cannot be negative")
    private BigDecimal ureaMgDl;
    
    // === HEMATOLOGÍA ===
    @DecimalMin(value = "0.0", message = "Hemoglobin level cannot be negative")
    private BigDecimal hemoglobinGDl;
    
    @DecimalMin(value = "0.0", message = "Hematocrit percentage cannot be negative")
    private BigDecimal hematocritPercent;
    
    @DecimalMin(value = "0.0", message = "Red blood cells count cannot be negative")
    private BigDecimal redBloodCellsMillionUl;
    
    @DecimalMin(value = "0.0", message = "White blood cells count cannot be negative")
    private BigDecimal whiteBloodCellsThousandUl;
    
    @DecimalMin(value = "0.0", message = "Platelets count cannot be negative")
    private BigDecimal plateletsThousandUl;
    
    // === FUNCIÓN HEPÁTICA ===
    @DecimalMin(value = "0.0", message = "ALT level cannot be negative")
    private BigDecimal altSgptUL;
    
    @DecimalMin(value = "0.0", message = "AST level cannot be negative")
    private BigDecimal astSgotUL;
    
    @DecimalMin(value = "0.0", message = "Bilirubin level cannot be negative")
    private BigDecimal bilirubinTotalMgDl;
    
    @DecimalMin(value = "0.0", message = "Alkaline phosphatase level cannot be negative")
    private BigDecimal alkalinePhosphataseUL;
    
    // === FUNCIÓN RENAL ===
    @DecimalMin(value = "0.0", message = "BUN level cannot be negative")
    private BigDecimal bunMgDl;
    
    @DecimalMin(value = "0.0", message = "GFR cannot be negative")
    private BigDecimal gfrMlMin;
    
    // === PROTEÍNAS ===
    @DecimalMin(value = "0.0", message = "Total protein level cannot be negative")
    private BigDecimal totalProteinGDl;
    
    @DecimalMin(value = "0.0", message = "Albumin level cannot be negative")
    private BigDecimal albuminGDl;
    
    // === ELECTROLITOS ===
    @DecimalMin(value = "0.0", message = "Sodium level cannot be negative")
    private BigDecimal sodiumMeqL;
    
    @DecimalMin(value = "0.0", message = "Potassium level cannot be negative")
    private BigDecimal potassiumMeqL;
    
    @DecimalMin(value = "0.0", message = "Chloride level cannot be negative")
    private BigDecimal chlorideMeqL;
    
    // === MARCADORES INFLAMATORIOS ===
    @DecimalMin(value = "0.0", message = "C-reactive protein level cannot be negative")
    private BigDecimal cReactiveProteinMgL;
    
    @DecimalMin(value = "0.0", message = "ESR cannot be negative")
    private BigDecimal esrMmHr;
    
    // === DATOS GENÉTICOS ===
    @Min(value = 0, message = "Genetic markers count cannot be negative")
    private Integer geneticMarkersDetected;
    
    @DecimalMin(value = "0.0", message = "Genetic quality score cannot be negative")
    private BigDecimal geneticQualityScore;
    
    // === METADATOS DEL LABORATORIO ===
    private String labReferenceValues; // JSON con valores de referencia
    private String analyzerModel;
    
    @Min(value = 0, message = "Centrifugation speed cannot be negative")
    private Integer centrifugationSpeedRpm;
    
    private Integer storageTemperatureCelsius;
}