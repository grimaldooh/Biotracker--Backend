package com.biotrack.backend.models.enums;

import com.biotrack.backend.dto.DnaExtractionMethodDTO;

public enum DnaExtractionMethod {
    PHENOL_CHLOROFORM,
    SILICA_COLUMN,
    MAGNETIC_BEADS,
    SALTING_OUT;

    public static DnaExtractionMethod fromDto(DnaExtractionMethodDTO dto) {
        if (dto == null) return null;
        return DnaExtractionMethod.valueOf(dto.name());
    }
}