package com.biotrack.backend.dto.insurance;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LambdaInsuranceResponseDTO {
    
    private BigDecimal monthlyPremium;
    private BigDecimal riskScore;
    private List<String> recommendations;
    private List<String> coverageDetails;
    
    // Breakdown detallado del cálculo
    private PremiumCalculationBreakdown breakdown;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PremiumCalculationBreakdown {
        private BigDecimal basePremium;
        private BigDecimal ageMultiplier;
        private BigDecimal healthMultiplier;
        private BigDecimal lifestyleMultiplier;
        private BigDecimal coverageMultiplier;
        private BigDecimal finalPremium;
    }
}