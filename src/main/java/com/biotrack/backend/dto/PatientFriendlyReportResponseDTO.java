package com.biotrack.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PatientFriendlyReportResponseDTO(
        @JsonProperty("patient_friendly_report")
        PatientFriendlyReport patientFriendlyReport
) {
    
    public record PatientFriendlyReport(
            @JsonProperty("your_test_summary")
            YourTestSummary yourTestSummary,
            
            @JsonProperty("what_your_results_mean")
            WhatYourResultsMean whatYourResultsMean,
            
            @JsonProperty("health_impact")
            HealthImpact healthImpact,
            
            @JsonProperty("next_steps")
            NextSteps nextSteps,
            
            @JsonProperty("questions_to_ask")
            QuestionsToAsk questionsToAsk,
            
            @JsonProperty("important_notes")
            ImportantNotes importantNotes
    ) {}
    
    public record YourTestSummary(
            @JsonProperty("what_was_tested")
            String whatWasTested,
            
            @JsonProperty("when_tested")
            String whenTested,
            
            @JsonProperty("main_findings")
            String mainFindings
    ) {}
    
    public record WhatYourResultsMean(
            @JsonProperty("in_simple_terms")
            String inSimpleTerms,
            
            @JsonProperty("what_is_normal")
            String whatIsNormal,
            
            @JsonProperty("your_specific_results")
            String yourSpecificResults
    ) {}
    
    public record HealthImpact(
            @JsonProperty("what_this_means_for_you")
            String whatThisMeansForYou,
            
            @JsonProperty("should_you_be_concerned")
            String shouldYouBeConcerned,
            
            @JsonProperty("positive_aspects")
            String positiveAspects
    ) {}
    
    public record NextSteps(
            @JsonProperty("immediate_actions")
            String immediateActions,
            
            @JsonProperty("lifestyle_tips")
            String lifestyleTips,
            
            @JsonProperty("follow_up_care")
            String followUpCare
    ) {}
    
    public record QuestionsToAsk(
            @JsonProperty("for_your_doctor")
            List<String> forYourDoctor,
            
            @JsonProperty("understanding_better")
            String understandingBetter
    ) {}
    
    public record ImportantNotes(
            String limitations,
            String remember
    ) {}
}