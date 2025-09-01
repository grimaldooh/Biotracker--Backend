package com.biotrack.backend.dto.insurance;

import com.biotrack.backend.models.enums.CoverageType;
import com.biotrack.backend.models.enums.QuoteStatus;
import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class InsuranceQuoteResponseDTO {
    
    private UUID quoteId;
    private UUID patientId;
    private String patientName;
    
    private BigDecimal monthlyPremium;
    private BigDecimal annualPremium;
    private BigDecimal riskScore;
    private CoverageType coverageType;
    private BigDecimal coverageAmount;
    private BigDecimal deductible;
    
    private QuoteStatus status;
    private LocalDateTime quoteDate;
    private LocalDateTime validUntil;
    
    private List<String> recommendations;
    private List<String> coverageDetails;
    
    // Breakdown del cálculo para transparencia
    private PremiumBreakdown premiumBreakdown;
    
    @Data
    @Builder
    public static class PremiumBreakdown {
        private BigDecimal basePremium;
        private BigDecimal ageAdjustment;
        private BigDecimal healthAdjustment;
        private BigDecimal lifestyleAdjustment;
        private BigDecimal coverageAdjustment;
        private BigDecimal finalPremium;
    }
}