package com.biotrack.backend.utils;

import com.biotrack.backend.dto.ResultFileDTO;
import com.biotrack.backend.models.ResultFile;

public class ResultFileMapper {
    
    public static ResultFileDTO toDTO(ResultFile resultFile) {
        return new ResultFileDTO(
                resultFile.getId(),
                resultFile.getFileName(),
                resultFile.getS3Key(),
                resultFile.getS3Url(),
                resultFile.getFileSize(),
                resultFile.getContentType(),
                resultFile.getProcessingStatus(),
                resultFile.getUploadedAt(),
                resultFile.getGeneticSample().getId() // ✅ CAMBIAR: usar geneticSample
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
