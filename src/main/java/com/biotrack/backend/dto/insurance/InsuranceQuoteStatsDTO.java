package com.biotrack.backend.dto.insurance;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InsuranceQuoteStatsDTO {
    
    private Long totalQuotes;
    private Long activeQuotes;
    private Long acceptedQuotes;
    private Long expiredQuotes;
    
    private BigDecimal averagePremium;
    private BigDecimal averageRiskScore;
    private BigDecimal averageCoverage;
    
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    
    // Estadísticas por tipo de cobertura
    private CoverageTypeStats basicStats;
    private CoverageTypeStats standardStats;
    private CoverageTypeStats premiumStats;
    private CoverageTypeStats comprehensiveStats;
    
    @Data
    @Builder
    public static class CoverageTypeStats {
        private Long count;
        private BigDecimal averagePremium;
        private BigDecimal averageRiskScore;
    }
}