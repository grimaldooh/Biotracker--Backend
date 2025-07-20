package com.biotrack.backend.dto;

public enum DnaExtractionMethodDTO {
    PHENOL_CHLOROFORM("Phenol-Chloroform"),
    SILICA_COLUMN("Silica Column"),
    MAGNETIC_BEADS("Magnetic Beads"),
    SALTING_OUT("Salting Out");
    
    private final String displayName;
    
    DnaExtractionMethodDTO(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}