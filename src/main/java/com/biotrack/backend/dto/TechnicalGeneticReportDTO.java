package com.biotrack.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TechnicalGeneticReportDTO(
        @JsonProperty("genetic_analysis_report")
        GeneticAnalysisReport geneticAnalysisReport
) {
    
    public record GeneticAnalysisReport(
            @JsonProperty("patient_summary")
            PatientSummary patientSummary,
            
            @JsonProperty("executive_summary")
            ExecutiveSummary executiveSummary,
            
            @JsonProperty("variant_analysis")
            List<VariantAnalysis> variantAnalysis,
            
            @JsonProperty("medical_history_correlation")
            MedicalHistoryCorrelation medicalHistoryCorrelation,
            
            @JsonProperty("clinical_implications")
            ClinicalImplications clinicalImplications,
            
            @JsonProperty("clinical_recommendations")
            ClinicalRecommendations clinicalRecommendations,
            
            @JsonProperty("technical_details")
            TechnicalDetails technicalDetails,
            
            @JsonProperty("follow_up_recommendations")
            FollowUpRecommendations followUpRecommendations,
            
            @JsonProperty("important_disclaimers")
            ImportantDisclaimers importantDisclaimers
    ) {}
    
    public record PatientSummary(
            String name,
            
            @JsonProperty("analysis_date")
            String analysisDate,
            
            @JsonProperty("total_variants_analyzed")
            Integer totalVariantsAnalyzed
    ) {}
    
    public record ExecutiveSummary(
            @JsonProperty("overall_risk_assessment")
            String overallRiskAssessment,
            
            @JsonProperty("key_findings")
            String keyFindings,
            
            @JsonProperty("clinical_priority")
            String clinicalPriority
    ) {}
    
    public record VariantAnalysis(
            String gene,
            String chromosome,
            
            @JsonProperty("variant_type")
            String variantType,
            
            @JsonProperty("pathogenicity_classification")
            String pathogenicityClassification,
            
            @JsonProperty("clinical_significance")
            String clinicalSignificance,
            
            @JsonProperty("population_frequency")
            String populationFrequency,
            
            @JsonProperty("inheritance_pattern")
            String inheritancePattern,
            
            @JsonProperty("associated_conditions")
            List<String> associatedConditions
    ) {}
    
    public record MedicalHistoryCorrelation(
            @JsonProperty("genetic_explanation_for_symptoms")
            GeneticExplanationForSymptoms geneticExplanationForSymptoms,
            
            @JsonProperty("laboratory_findings_correlation")
            LaboratoryFindingsCorrelation laboratoryFindingsCorrelation,
            
            @JsonProperty("progression_pattern_analysis")
            ProgressionPatternAnalysis progressionPatternAnalysis,
            
            @JsonProperty("unexplained_findings")
            UnexplainedFindings unexplainedFindings,
            
            @JsonProperty("genetic_predisposition_confirmation")
            GeneticPredispositionConfirmation geneticPredispositionConfirmation,
            
            @JsonProperty("family_history_implications")
            String familyHistoryImplications
    ) {}
    
    public record GeneticExplanationForSymptoms(
            String analysis,
            
            @JsonProperty("referenced_sample_evidence")
            List<ReferencedSampleEvidence> referencedSampleEvidence
    ) {}
    
    public record ReferencedSampleEvidence(
            @JsonProperty("sample_id")
            String sampleId,
            
            String finding
    ) {}
    
    public record LaboratoryFindingsCorrelation(
            String analysis,
            
            @JsonProperty("referenced_sample_ids")
            List<String> referencedSampleIds
    ) {}
    
    public record ProgressionPatternAnalysis(
            String analysis,
            
            @JsonProperty("referenced_medical_visits")
            List<String> referencedMedicalVisits
    ) {}
    
    public record UnexplainedFindings(
            String analysis,
            
            @JsonProperty("referenced_sample_ids")
            List<String> referencedSampleIds,
            
            @JsonProperty("additional_testing_needed")
            String additionalTestingNeeded
    ) {}
    
    public record GeneticPredispositionConfirmation(
            String analysis,
            
            @JsonProperty("supporting_evidence")
            List<String> supportingEvidence,
            
            @JsonProperty("contradicting_evidence")
            List<String> contradictingEvidence
    ) {}
    
    public record ClinicalImplications(
            @JsonProperty("disease_risk")
            String diseaseRisk,
            
            @JsonProperty("phenotypic_manifestations")
            String phenotypicManifestations,
            
            @JsonProperty("penetrance_information")
            String penetranceInformation,
            
            @JsonProperty("age_of_onset_considerations")
            String ageOfOnsetConsiderations
    ) {}
    
    public record ClinicalRecommendations(
            @JsonProperty("immediate_actions")
            List<String> immediateActions,
            
            @JsonProperty("monitoring_schedule")
            String monitoringSchedule,
            
            @JsonProperty("therapeutic_considerations")
            String therapeuticConsiderations,
            
            @JsonProperty("lifestyle_modifications")
            List<String> lifestyleModifications,
            
            @JsonProperty("family_screening")
            String familyScreening,
            
            @JsonProperty("genetic_counseling")
            String geneticCounseling
    ) {}
    
    public record TechnicalDetails(
            String methodology,
            
            @JsonProperty("coverage_limitations")
            String coverageLimitations,
            
            @JsonProperty("variant_interpretation_databases")
            List<String> variantInterpretationDatabases,
            
            @JsonProperty("analysis_limitations")
            String analysisLimitations
    ) {}
    
    public record FollowUpRecommendations(
            @JsonProperty("additional_testing")
            String additionalTesting,
            
            @JsonProperty("specialist_referrals")
            List<String> specialistReferrals,
            
            @JsonProperty("reanalysis_timeline")
            String reanalysisTimeline
    ) {}
    
    public record ImportantDisclaimers(
            @JsonProperty("interpretation_certainty")
            String interpretationCertainty,
            
            @JsonProperty("evolving_knowledge")
            String evolvingKnowledge,
            
            @JsonProperty("clinical_correlation")
            String clinicalCorrelation,
            
            @JsonProperty("evidence_limitations")
            String evidenceLimitations
    ) {}
}