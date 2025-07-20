package com.biotrack.backend.utils;

import com.biotrack.backend.dto.ClinicalHistoryRecordDTO;
import com.biotrack.backend.models.ClinicalHistoryRecord;

public class ClinicalHistoryRecordMapper {
    public static ClinicalHistoryRecordDTO toDTO(ClinicalHistoryRecord record) {
        ClinicalHistoryRecordDTO dto = new ClinicalHistoryRecordDTO();
        dto.setId(record.getId());
        dto.setPatientId(record.getPatient().getId());
        dto.setS3Url(record.getS3Url());
        dto.setCreatedAt(record.getCreatedAt());
        return dto;
    }
}