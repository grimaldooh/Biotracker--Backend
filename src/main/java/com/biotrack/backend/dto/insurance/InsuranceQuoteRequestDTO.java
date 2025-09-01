
package com.biotrack.backend.dto.insurance;

import com.biotrack.backend.models.enums.CoverageType;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InsuranceQuoteRequestDTO {
    
    @NotNull(message = "Patient ID is required")
    private UUID patientId;
    
    @NotNull(message = "Coverage type is required")
    private CoverageType coverageType;
    
    @NotNull(message = "Coverage amount is required")
    @DecimalMin(value = "10000.00", message = "Coverage amount must be at least $10,000")
    @DecimalMax(value = "10000000.00", message = "Coverage amount cannot exceed $10,000,000")
    private BigDecimal coverageAmount;
    
    @NotNull(message = "Deductible is required")
    @DecimalMin(value = "500.00", message = "Deductible must be at least $500")
    @DecimalMax(value = "50000.00", message = "Deductible cannot exceed $50,000")
    private BigDecimal deductible;
    
    // Datos adicionales del estilo de vida
    @Min(value = 1, message = "Lifestyle score must be between 1 and 10")
    @Max(value = 10, message = "Lifestyle score must be between 1 and 10")
    private Integer lifestyleScore; // 1-10 (1=muy malo, 10=excelente)
    
    @Min(value = 1, message = "Occupation risk level must be between 1 and 5")
    @Max(value = 5, message = "Occupation risk level must be between 1 and 5")
    private Integer occupationRiskLevel; // 1-5 (1=muy bajo, 5=muy alto)
    
    @Min(value = 1, message = "Family history risk must be between 1 and 5")
    @Max(value = 5, message = "Family history risk must be between 1 and 5")
    private Integer familyHistoryRisk; // 1-5
    
    @Min(value = 0, message = "Exercise frequency must be between 0 and 7")
    @Max(value = 7, message = "Exercise frequency must be between 0 and 7")
    private Integer exerciseFrequency; // días por semana
    
    @NotNull(message = "Smoking status is required")
    private Boolean smokingStatus;
    
    @Min(value = 0, message = "Alcohol consumption must be between 0 and 10")
    @Max(value = 10, message = "Alcohol consumption must be between 0 and 10")
    private Integer alcoholConsumption; // bebidas por semana
}
