package com.biotrack.backend.services;

import com.biotrack.backend.dto.MedicationAnalysisDTO;
import com.biotrack.backend.models.Mutation;

import java.util.List;

public interface OpenAIService {
    String generateGeneticReport(List<Mutation> mutations, String patientInfo);
    String generateClinicalReport(String patientInfo);
    String generatePatientFriendlyClinicalReport(String patientInfo);
    String generateClinicalHistorySummary(String prompt);
    String generatePatientFriendlyGeneticReport(List<Mutation> mutations, String clinicalContext, String technicalReport);

    String generateMedicationCompatibilityReport(List<MedicationAnalysisDTO> medications, String clinicalContext);
    
    boolean isConfigured();
    String getModelUsed();
}