package com.biotrack.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MedicalStudyReportResponseDTO(
        @JsonProperty("medical_study_report")
        MedicalStudyReport medicalStudyReport
) {
    
    public record MedicalStudyReport(
            @JsonProperty("patient_data")
            PatientData patientData,
            
            @JsonProperty("sample_information")
            SampleInformation sampleInformation,
            
            @JsonProperty("clinical_findings")
            ClinicalFindings clinicalFindings,
            
            RecommendationsData recommendations
    ) {}
    
    public record PatientData(
            String name
    ) {}
    
    public record SampleInformation(
            @JsonProperty("sample_type")
            String sampleType,
            
            @JsonProperty("analyzer_model")
            String analyzerModel,
            
            @JsonProperty("collection_date")
            String collectionDate,
            
            @JsonProperty("lab_notes")
            String labNotes
    ) {}
    
    public record ClinicalFindings(
            @JsonProperty("executive_summary")
            String executiveSummary,
            
            @JsonProperty("sample_analysis")
            String sampleAnalysis,
            
            @JsonProperty("clinical_significance")
            String clinicalSignificance
    ) {}
    
    public record RecommendationsData(
            @JsonProperty("action_plan")
            String actionPlan,
            
            @JsonProperty("analysis_limitations")
            String analysisLimitations
    ) {}
}