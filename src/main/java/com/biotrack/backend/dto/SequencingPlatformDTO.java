package com.biotrack.backend.dto;

public enum SequencingPlatformDTO {
    ILLUMINA_NOVASEQ("Illumina NovaSeq"),
    ILLUMINA_HISEQ("Illumina HiSeq"),
    PACBIO_SEQUEL("PacBio Sequel"),
    OXFORD_NANOPORE("Oxford Nanopore"),
    ION_TORRENT("Ion Torrent");
    
    private final String displayName;
    
    SequencingPlatformDTO(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}