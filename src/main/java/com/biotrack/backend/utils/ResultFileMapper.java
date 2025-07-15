package com.biotrack.backend.utils;

import com.biotrack.backend.dto.ResultFileDTO;
import com.biotrack.backend.models.ResultFile;

public class ResultFileMapper {
    public static ResultFileDTO toDTO(ResultFile file) {
        return new ResultFileDTO(
                file.getId(),
                file.getFileName(), 
                file.getS3Url(),
                file.getUploadedAt(),
                file.getSample().getId()
        );
    }

    public static ResultFile toEntity(ResultFileDTO dto) {
        return ResultFile.builder()
                .id(dto.id())
                .fileName(dto.fileName()) 
                .s3Url(dto.s3Url())
                .uploadedAt(dto.uploadedAt())
                // Nota: Sample debe ser asignado por separado ya que necesita el ID
                .build();
    }
}
