package com.biotrack.backend.models.enums;

public enum MedicalVisitType {
    CONSULTATION("Consultation"),
    FOLLOW_UP("Follow Up"),
    SURGERY("Surgery"),
    EMERGENCY("Emergency"),
    OTHER("Other");

    private final String displayName;

    MedicalVisitType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}