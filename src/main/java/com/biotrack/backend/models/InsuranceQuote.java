package com.biotrack.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.biotrack.backend.models.enums.CoverageType;
import com.biotrack.backend.models.enums.QuoteStatus;

@Entity
@Table(name = "insurance_quotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceQuote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
    
    @Column(name = "monthly_premium", precision = 10, scale = 2, nullable = false)
    private BigDecimal monthlyPremium;
    
    @Column(name = "risk_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal riskScore;
    
    @Column(name = "coverage_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal coverageAmount;
    
    @Column(name = "deductible", precision = 10, scale = 2, nullable = false)
    private BigDecimal deductible;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_type", nullable = false)
    private CoverageType coverageType;
    
    @Column(name = "lifestyle_score", nullable = false)
    private Integer lifestyleScore;
    
    @Column(name = "occupation_risk_level", nullable = false)
    private Integer occupationRiskLevel;
    
    @Column(name = "family_history_risk", nullable = false)
    private Integer familyHistoryRisk;
    
    @Column(name = "exercise_frequency", nullable = false)
    private Integer exerciseFrequency;
    
    @Column(name = "smoking_status", nullable = false)
    private Boolean smokingStatus;
    
    @Column(name = "alcohol_consumption", nullable = false)
    private Integer alcoholConsumption;
    
    @Column(name = "quote_date", nullable = false)
    private LocalDateTime quoteDate;
    
    @Column(name = "valid_until", nullable = false)
    private LocalDateTime validUntil;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private QuoteStatus status;
    
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;
    
    @PrePersist
    protected void onCreate() {
        quoteDate = LocalDateTime.now();
        validUntil = LocalDateTime.now().plusDays(30); // Válida por 30 días
        status = QuoteStatus.ACTIVE;
    }
}