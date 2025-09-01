package com.biotrack.backend.dto.insurance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LambdaInsuranceRequestDTO {
    
    // Campos requeridos
    @JsonProperty("age")
    private Integer age;
    
    @JsonProperty("coverageAmount")
    private Double coverageAmount;
    
    // Campos opcionales
    @JsonProperty("coverageType")
    private String coverageType;
    
    @JsonProperty("chronicConditionsCount")
    private Integer chronicConditionsCount;
    
    @JsonProperty("smokingStatus")
    private Boolean smokingStatus;
    
    @JsonProperty("lifestyleScore")
    private Integer lifestyleScore;
    
    @JsonProperty("exerciseFrequency")
    private Integer exerciseFrequency;
    
    @JsonProperty("occupationRiskLevel")
    private Integer occupationRiskLevel;
    
    @JsonProperty("familyHistoryRisk")
    private Integer familyHistoryRisk;
    
    @JsonProperty("consultationsPerYear")
    private Integer consultationsPerYear;
    
    @JsonProperty("medicationsCount")
    private Integer medicationsCount;
    
    @JsonProperty("averageLabResults")
    private Double averageLabResults;
    
    @JsonProperty("alcoholConsumption")
    private Integer alcoholConsumption;
}