package com.biotrack.backend.dto.insurance;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@Builder
public class LambdaInsuranceRequestDTO {
    
    // Datos del paciente (del PatientSummary)
    private Integer age;
    private Double bmi;
    private Integer chronicConditionsCount;
    private Integer consultationsPerYear;
    private Integer medicationsCount;
    private Double averageLabResults; // promedio normalizado de resultados críticos
    
    // Datos del request (lifestyle)
    private Integer lifestyleScore;
    private Integer occupationRiskLevel;
    private Integer familyHistoryRisk;
    private Integer exerciseFrequency;
    private Boolean smokingStatus;
    private Integer alcoholConsumption;
    
    // Datos de la cobertura solicitada
    private String coverageType;
    private BigDecimal coverageAmount;
    private BigDecimal deductible;
}