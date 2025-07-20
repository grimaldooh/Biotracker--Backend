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
public class SalivaSampleDataDTO {
    
    @DecimalMin(value = "0.1", message = "Volume must be at least 0.1 mL")
    private BigDecimal volumeMl;
    
    @DecimalMin(value = "5.0", message = "pH level must be at least 5.0")
    private BigDecimal phLevel;
    
    private String viscosity; // "Low", "Medium", "High"
    
    @DecimalMin(value = "0.0", message = "DNA yield cannot be negative")
    private BigDecimal dnaYieldNg;
    
    @Min(value = 0, message = "Cell count cannot be negative")
    private Integer cellCountPerMl;
    
    private SalivaCollectionMethodDTO collectionMethod;
    
    private Boolean fastingStatus;
    
    private String contaminationLevel; // "None", "Low", "Medium", "High"
    
    private String preservativeUsed;
    
    @Min(value = 0, message = "Processing time cannot be negative")
    private Integer timeToProcessingHours;
}