package com.biotrack.backend.models.enums;

public enum QuoteStatus {
    ACTIVE("Activa"),
    EXPIRED("Expirada"),
    ACCEPTED("Aceptada"),
    REJECTED("Rechazada"),
    PENDING("Pendiente");
    
    private final String displayName;
    
    QuoteStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}