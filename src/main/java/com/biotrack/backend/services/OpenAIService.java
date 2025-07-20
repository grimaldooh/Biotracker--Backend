package com.biotrack.backend.services;

import com.biotrack.backend.models.Mutation;

import java.util.List;

public interface OpenAIService {
    String generateGeneticReport(List<Mutation> mutations, String patientInfo);
    String generateClinicalReport(String patientInfo);
    boolean isConfigured();
    String getModelUsed();
    String generateClinicalHistorySummary(String prompt);
}