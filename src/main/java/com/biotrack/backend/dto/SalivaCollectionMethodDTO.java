package com.biotrack.backend.dto;

public enum SalivaCollectionMethodDTO {
    PASSIVE_DROOL("Passive Drool"),
    SPIT_TUBE("Spit Tube"),
    SWAB_COLLECTION("Swab Collection"),
    ORAGENE_KIT("Oragene Kit");
    
    private final String displayName;
    
    SalivaCollectionMethodDTO(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}