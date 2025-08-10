package com.biotrack.backend.dto;

import java.util.UUID;

public record PatientReportsDTO(
        UUID sampleId,
        String s3Url,        
        String s3UrlPatient  
) {}