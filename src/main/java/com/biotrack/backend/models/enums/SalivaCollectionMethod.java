package com.biotrack.backend.models.enums;

import com.biotrack.backend.dto.SalivaCollectionMethodDTO;

public enum SalivaCollectionMethod {
    PASSIVE_DROOL,
    SPIT_TUBE,
    SWAB_COLLECTION,
    ORAGENE_KIT;

    public static SalivaCollectionMethod fromDto(SalivaCollectionMethodDTO dto) {
        if (dto == null) return null;
        return SalivaCollectionMethod.valueOf(dto.name());
    }
}