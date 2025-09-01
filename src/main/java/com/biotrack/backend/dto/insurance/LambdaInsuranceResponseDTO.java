package com.biotrack.backend.dto.insurance;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class LambdaInsuranceResponseDTO {
    
    @JsonProperty("monthlyPremium")
    private Double monthlyPremium;
    
    @JsonProperty("riskScore")
    private Double riskScore;
    
    @JsonProperty("modelVersion")
    private String modelVersion;
    
    @JsonProperty("fallbackMode")
    private Boolean fallbackMode;
    
    @JsonProperty("architecture")
    private String architecture;
    
    @JsonProperty("pythonVersion")
    private String pythonVersion;
    
    @JsonProperty("processingTime")
    private Double processingTime;
    
    @JsonProperty("recommendations")
    private List<String> recommendations;
    
    @JsonProperty("breakdown")
    private PremiumBreakdown breakdown;
    
    @JsonProperty("timestamp")
    private String timestamp;
    
    // Error fields
    @JsonProperty("error")
    private Boolean error;
    
    @JsonProperty("message")
    private String message;
    
    @Data
    @NoArgsConstructor
    public static class PremiumBreakdown {
        @JsonProperty("basePremium")
        private Double basePremium;
        
        @JsonProperty("ageFactor")
        private Double ageFactor;
        
        @JsonProperty("healthFactor")
        private Double healthFactor;
        
        @JsonProperty("smokingFactor")
        private Double smokingFactor;
        
        @JsonProperty("lifestyleFactor")
        private Double lifestyleFactor;
        
        @JsonProperty("exerciseFactor")
        private Double exerciseFactor;
    }
}