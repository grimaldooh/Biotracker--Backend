package com.biotrack.backend.models.enums;

public enum CoverageType {
    BASIC("Cobertura Básica"),
    STANDARD("Cobertura Estándar"),
    PREMIUM("Cobertura Premium"),
    COMPREHENSIVE("Cobertura Integral");
    
    private final String displayName;
    
    CoverageType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}