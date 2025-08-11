package com.biotrack.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record PatientFriendlyGeneticReportDTO(
        @JsonProperty("your_genetic_report")
        YourGeneticReport yourGeneticReport
) {
    
    public record YourGeneticReport(
            @JsonProperty("understanding_your_test")
            UnderstandingYourTest understandingYourTest,
            
            @JsonProperty("your_genetic_findings")
            YourGeneticFindings yourGeneticFindings,
            
            @JsonProperty("your_specific_variants")
            List<YourSpecificVariant> yourSpecificVariants,
            
            @JsonProperty("health_implications")
            HealthImplications healthImplications,
            
            @JsonProperty("family_considerations")
            FamilyConsiderations familyConsiderations,
            
            @JsonProperty("your_action_plan")
            YourActionPlan yourActionPlan,
            
            @JsonProperty("understanding_genetics")
            UnderstandingGenetics understandingGenetics,
            
            @JsonProperty("questions_and_support")
            QuestionsAndSupport questionsAndSupport,
            
            @JsonProperty("important_reminders")
            ImportantReminders importantReminders,
            
            @JsonProperty("next_steps_summary")
            NextStepsSummary nextStepsSummary
    ) {}
    
    public record UnderstandingYourTest(
            @JsonProperty("what_we_analyzed")
            String whatWeAnalyzed,
            
            @JsonProperty("test_date")
            String testDate,
            
            @JsonProperty("total_variants_found")
            Integer totalVariantsFound,
            
            @JsonProperty("main_message")
            String mainMessage
    ) {}
    
    public record YourGeneticFindings(
            @JsonProperty("overall_picture")
            String overallPicture,
            
            @JsonProperty("risk_level")
            String riskLevel,
            
            @JsonProperty("what_this_means_for_you")
            String whatThisMeansForYou,
            
            @JsonProperty("comparison_to_others")
            String comparisonToOthers
    ) {}
    
    public record YourSpecificVariant(
            @JsonProperty("gene_name")
            String geneName,
            
            @JsonProperty("what_this_gene_does")
            String whatThisGeneDoes,
            
            @JsonProperty("your_variant")
            String yourVariant,
            
            @JsonProperty("what_it_means")
            String whatItMeans,
            
            @JsonProperty("how_common_is_it")
            String howCommonIsIt,
            
            @JsonProperty("inheritance_info")
            String inheritanceInfo,
            
            @JsonProperty("concern_level")
            String concernLevel,
            
            @JsonProperty("action_needed")
            String actionNeeded
    ) {}
    
    public record HealthImplications(
            @JsonProperty("immediate_concerns")
            String immediateConcerns,
            
            @JsonProperty("long_term_outlook")
            String longTermOutlook,
            
            @JsonProperty("lifestyle_impact")
            String lifestyleImpact,
            
            @JsonProperty("preventive_opportunities")
            String preventiveOpportunities
    ) {}
    
    public record FamilyConsiderations(
            @JsonProperty("family_risk")
            String familyRisk,
            
            @JsonProperty("inheritance_pattern")
            String inheritancePattern,
            
            @JsonProperty("family_testing_recommendations")
            String familyTestingRecommendations,
            
            @JsonProperty("children_considerations")
            String childrenConsiderations
    ) {}
    
    public record YourActionPlan(
            @JsonProperty("immediate_steps")
            List<ImmediateStep> immediateSteps,
            
            @JsonProperty("lifestyle_recommendations")
            List<LifestyleRecommendation> lifestyleRecommendations,
            
            @JsonProperty("medical_monitoring")
            MedicalMonitoring medicalMonitoring
    ) {}
    
    public record ImmediateStep(
            String action,
            
            @JsonProperty("why_important")
            String whyImportant,
            
            String timeline
    ) {}
    
    public record LifestyleRecommendation(
            String category,
            String recommendation,
            String benefit
    ) {}
    
    public record MedicalMonitoring(
            @JsonProperty("tests_to_discuss")
            List<String> testsToDiscuss,
            
            String frequency,
            
            @JsonProperty("specialists_to_see")
            List<String> specialistsToSee
    ) {}
    
    public record UnderstandingGenetics(
            @JsonProperty("genetics_101")
            String genetics101,
            
            @JsonProperty("why_testing_matters")
            String whyTestingMatters,
            
            @JsonProperty("limitations_to_know")
            String limitationsToKnow,
            
            @JsonProperty("future_discoveries")
            String futureDiscoveries
    ) {}
    
    public record QuestionsAndSupport(
            @JsonProperty("questions_for_doctor")
            List<String> questionsForDoctor,
            
            @JsonProperty("questions_for_genetic_counselor")
            List<String> questionsForGeneticCounselor,
            
            @JsonProperty("emotional_support")
            String emotionalSupport,
            
            @JsonProperty("resources_to_explore")
            List<String> resourcesToExplore
    ) {}
    
    public record ImportantReminders(
            @JsonProperty("genetics_is_not_destiny")
            String geneticsIsNotDestiny,
            
            @JsonProperty("lifestyle_matters")
            String lifestyleMatters,
            
            @JsonProperty("ongoing_relationship")
            String ongoingRelationship,
            
            @JsonProperty("privacy_and_discrimination")
            String privacyAndDiscrimination
    ) {}
    
    public record NextStepsSummary(
            @JsonProperty("most_important_action")
            String mostImportantAction,
            
            @JsonProperty("timeline_overview")
            String timelineOverview,
            
            @JsonProperty("follow_up_plan")
            String followUpPlan
    ) {}
}