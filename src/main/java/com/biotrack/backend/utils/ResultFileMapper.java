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
}
