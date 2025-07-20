package com.biotrack.backend.models.enums;

import com.biotrack.backend.dto.SequencingPlatformDTO;

public enum SequencingPlatform {
    ILLUMINA_NOVASEQ,
    ILLUMINA_HISEQ,
    PACBIO_SEQUEL,
    OXFORD_NANOPORE,
    ION_TORRENT;

    public static SequencingPlatform fromDto(SequencingPlatformDTO dto) {
        if (dto == null) return null;
        return SequencingPlatform.valueOf(dto.name());
    }

}
