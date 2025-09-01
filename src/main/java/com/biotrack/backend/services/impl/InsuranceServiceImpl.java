package com.biotrack.backend.services.impl;

import com.biotrack.backend.dto.insurance.*;
import com.biotrack.backend.models.InsuranceQuote;
import com.biotrack.backend.models.Patient;
import com.biotrack.backend.models.enums.CoverageType;
import com.biotrack.backend.models.enums.QuoteStatus;
import com.biotrack.backend.repositories.InsuranceQuoteRepository;
import com.biotrack.backend.repositories.PatientRepository;
import com.biotrack.backend.services.InsuranceService;
import com.biotrack.backend.services.AwsLambdaInsuranceService;
import com.biotrack.backend.services.PatientService;
import com.biotrack.backend.services.impl.MedicalSummaryParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InsuranceServiceImpl implements InsuranceService {

    private final InsuranceQuoteRepository insuranceQuoteRepository;
    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final AwsLambdaInsuranceService lambdaInsuranceService;
    private final MedicalSummaryParserService medicalSummaryParserService;

    @Override
    public InsuranceQuoteResponseDTO calculateInsuranceQuote(InsuranceQuoteRequestDTO request) {
        log.info("Starting insurance quote calculation for patient: {}", request.getPatientId());
        
        try {
            // 1. Validar y obtener paciente
            Patient patient = validateAndGetPatient(request.getPatientId());
            
            // 2. Obtener y parsear resumen médico del paciente
            MedicalDataForInsuranceDTO medicalData = getPatientMedicalData(patient.getId());
            
            // 3. Preparar datos para AWS Lambda
            LambdaInsuranceRequestDTO lambdaRequest = prepareLambdaRequest(request, medicalData);
            
            // 4. Llamar a AWS Lambda para cálculo ML
            LambdaInsuranceResponseDTO lambdaResponse = lambdaInsuranceService.calculateInsurancePremium(lambdaRequest);
            
            // 5. Crear y guardar la cotización
            InsuranceQuote quote = createInsuranceQuote(request, patient, lambdaResponse);
            InsuranceQuote savedQuote = insuranceQuoteRepository.save(quote);
            
            // 6. Convertir a DTO de respuesta
            InsuranceQuoteResponseDTO response = mapToResponseDTO(savedQuote, lambdaResponse);
            
            log.info("Insurance quote calculated successfully. Quote ID: {}, Premium: ${}", 
                    savedQuote.getId(), savedQuote.getMonthlyPremium());
            
            return response;
            
        } catch (Exception e) {
            log.error("Error calculating insurance quote for patient: {}", request.getPatientId(), e);
            throw new RuntimeException("Failed to calculate insurance quote", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceQuoteResponseDTO> getPatientQuotes(UUID patientId) {
        log.info("Fetching insurance quotes for patient: {}", patientId);
        
        List<InsuranceQuote> quotes = insuranceQuoteRepository.findByPatientIdOrderByQuoteDateDesc(patientId);
        
        return quotes.stream()
                .map(this::mapToBasicResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceQuoteResponseDTO getLatestActiveQuote(UUID patientId) {
        log.info("Fetching latest active quote for patient: {}", patientId);
        
        return insuranceQuoteRepository
                .findTopByPatientIdAndStatusOrderByQuoteDateDesc(patientId, QuoteStatus.ACTIVE)
                .map(this::mapToBasicResponseDTO)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceQuoteResponseDTO getQuoteById(UUID quoteId) {
        log.info("Fetching quote by ID: {}", quoteId);
        
        InsuranceQuote quote = insuranceQuoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Insurance quote not found with ID: " + quoteId));
        
        return mapToBasicResponseDTO(quote);
    }

    @Override
    public InsuranceQuoteResponseDTO acceptQuote(UUID quoteId) {
        log.info("Accepting insurance quote: {}", quoteId);
        
        InsuranceQuote quote = insuranceQuoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Insurance quote not found with ID: " + quoteId));
        
        if (quote.getStatus() != QuoteStatus.ACTIVE) {
            throw new RuntimeException("Quote is not in ACTIVE status and cannot be accepted");
        }
        
        if (quote.getValidUntil().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Quote has expired and cannot be accepted");
        }
        
        quote.setStatus(QuoteStatus.ACCEPTED);
        InsuranceQuote savedQuote = insuranceQuoteRepository.save(quote);
        
        log.info("Insurance quote accepted successfully: {}", quoteId);
        return mapToBasicResponseDTO(savedQuote);
    }

    @Override
    public InsuranceQuoteResponseDTO rejectQuote(UUID quoteId) {
        log.info("Rejecting insurance quote: {}", quoteId);
        
        InsuranceQuote quote = insuranceQuoteRepository.findById(quoteId)
                .orElseThrow(() -> new RuntimeException("Insurance quote not found with ID: " + quoteId));
        
        quote.setStatus(QuoteStatus.REJECTED);
        InsuranceQuote savedQuote = insuranceQuoteRepository.save(quote);
        
        log.info("Insurance quote rejected successfully: {}", quoteId);
        return mapToBasicResponseDTO(savedQuote);
    }

    @Override
    public void markExpiredQuotes() {
        log.info("Marking expired insurance quotes");
        
        List<InsuranceQuote> expiredQuotes = insuranceQuoteRepository
                .findExpiredQuotes(LocalDateTime.now(), QuoteStatus.ACTIVE);
        
        expiredQuotes.forEach(quote -> quote.setStatus(QuoteStatus.EXPIRED));
        insuranceQuoteRepository.saveAll(expiredQuotes);
        
        log.info("Marked {} quotes as expired", expiredQuotes.size());
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceQuoteStatsDTO getQuoteStatistics() {
        log.info("Generating insurance quote statistics");
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        Long totalQuotes = insuranceQuoteRepository.countQuotesSince(thirtyDaysAgo);
        Double averagePremium = insuranceQuoteRepository.getAveragePremiumSince(thirtyDaysAgo);
        
        // Contar por status
        long activeQuotes = insuranceQuoteRepository.countByStatusAndQuoteDateAfter(QuoteStatus.ACTIVE, thirtyDaysAgo);
        long acceptedQuotes = insuranceQuoteRepository.countByStatusAndQuoteDateAfter(QuoteStatus.ACCEPTED, thirtyDaysAgo);
        long expiredQuotes = insuranceQuoteRepository.countByStatusAndQuoteDateAfter(QuoteStatus.EXPIRED, thirtyDaysAgo);
        
        return InsuranceQuoteStatsDTO.builder()
                .totalQuotes(totalQuotes)
                .activeQuotes(activeQuotes)
                .acceptedQuotes(acceptedQuotes)
                .expiredQuotes(expiredQuotes)
                .averagePremium(averagePremium != null ? BigDecimal.valueOf(averagePremium) : BigDecimal.ZERO)
                .periodStart(thirtyDaysAgo)
                .periodEnd(LocalDateTime.now())
                .build();
    }

    // Métodos privados auxiliares

    private Patient validateAndGetPatient(UUID patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + patientId));
    }

    private MedicalDataForInsuranceDTO getPatientMedicalData(UUID patientId) {
        try {
            String summaryJson = patientService.getLatestSummaryText(patientId);
            return medicalSummaryParserService.parseMedicalSummary(summaryJson);
        } catch (Exception e) {
            log.warn("Could not fetch patient summary for patient: {}. Using default values.", patientId, e);
            return createDefaultMedicalData();
        }
    }

    private MedicalDataForInsuranceDTO createDefaultMedicalData() {
        return MedicalDataForInsuranceDTO.builder()
                .age(35)
                .chronicConditionsCount(0)
                .consultationsThisYear(1)
                .medicationsCount(0)
                .recentStudiesCount(0)
                .hasAbnormalLabResults(false)
                .riskFactorsFromHistory(List.of())
                .build();
    }

    private LambdaInsuranceRequestDTO prepareLambdaRequest(InsuranceQuoteRequestDTO request, MedicalDataForInsuranceDTO medicalData) {
        return LambdaInsuranceRequestDTO.builder()
                // Datos del resumen médico
                .age(medicalData.getAge())
                .bmi(null) // No disponible en el JSON actual
                .chronicConditionsCount(medicalData.getChronicConditionsCount())
                .consultationsPerYear(medicalData.getConsultationsThisYear())
                .medicationsCount(medicalData.getMedicationsCount())
                .averageLabResults(medicalData.getHasAbnormalLabResults() ? 1.5 : 0.5) // Normalizado
                
                // Datos del request (lifestyle)
                .lifestyleScore(request.getLifestyleScore())
                .occupationRiskLevel(request.getOccupationRiskLevel())
                .familyHistoryRisk(request.getFamilyHistoryRisk())
                .exerciseFrequency(request.getExerciseFrequency())
                .smokingStatus(request.getSmokingStatus())
                .alcoholConsumption(request.getAlcoholConsumption())
                
                // Datos de cobertura
                .coverageType(request.getCoverageType().name())
                .coverageAmount(request.getCoverageAmount())
                .deductible(request.getDeductible())
                .build();
    }

    private InsuranceQuote createInsuranceQuote(InsuranceQuoteRequestDTO request, Patient patient, LambdaInsuranceResponseDTO lambdaResponse) {
        return InsuranceQuote.builder()
                .patient(patient)
                .monthlyPremium(lambdaResponse.getMonthlyPremium().setScale(2, RoundingMode.HALF_UP))
                .riskScore(lambdaResponse.getRiskScore().setScale(2, RoundingMode.HALF_UP))
                .coverageAmount(request.getCoverageAmount())
                .deductible(request.getDeductible())
                .coverageType(request.getCoverageType())
                .lifestyleScore(request.getLifestyleScore())
                .occupationRiskLevel(request.getOccupationRiskLevel())
                .familyHistoryRisk(request.getFamilyHistoryRisk())
                .exerciseFrequency(request.getExerciseFrequency())
                .smokingStatus(request.getSmokingStatus())
                .alcoholConsumption(request.getAlcoholConsumption())
                .recommendations(String.join("|", lambdaResponse.getRecommendations()))
                .build();
    }

    private InsuranceQuoteResponseDTO mapToResponseDTO(InsuranceQuote quote, LambdaInsuranceResponseDTO lambdaResponse) {
        return InsuranceQuoteResponseDTO.builder()
                .quoteId(quote.getId())
                .patientId(quote.getPatient().getId())
                .patientName(quote.getPatient().getFirstName())
                .monthlyPremium(quote.getMonthlyPremium())
                .annualPremium(quote.getMonthlyPremium().multiply(BigDecimal.valueOf(12)))
                .riskScore(quote.getRiskScore())
                .coverageType(quote.getCoverageType())
                .coverageAmount(quote.getCoverageAmount())
                .deductible(quote.getDeductible())
                .status(quote.getStatus())
                .quoteDate(quote.getQuoteDate())
                .validUntil(quote.getValidUntil())
                .recommendations(lambdaResponse.getRecommendations())
                .coverageDetails(lambdaResponse.getCoverageDetails())
                .premiumBreakdown(mapBreakdown(lambdaResponse.getBreakdown()))
                .build();
    }

    private InsuranceQuoteResponseDTO mapToBasicResponseDTO(InsuranceQuote quote) {
        return InsuranceQuoteResponseDTO.builder()
                .quoteId(quote.getId())
                .patientId(quote.getPatient().getId())
                .patientName(quote.getPatient().getFirstName())
                .monthlyPremium(quote.getMonthlyPremium())
                .annualPremium(quote.getMonthlyPremium().multiply(BigDecimal.valueOf(12)))
                .riskScore(quote.getRiskScore())
                .coverageType(quote.getCoverageType())
                .coverageAmount(quote.getCoverageAmount())
                .deductible(quote.getDeductible())
                .status(quote.getStatus())
                .quoteDate(quote.getQuoteDate())
                .validUntil(quote.getValidUntil())
                .recommendations(quote.getRecommendations() != null ? 
                    List.of(quote.getRecommendations().split("\\|")) : List.of())
                .build();
    }

    private InsuranceQuoteResponseDTO.PremiumBreakdown mapBreakdown(LambdaInsuranceResponseDTO.PremiumCalculationBreakdown breakdown) {
        if (breakdown == null) {
            return null;
        }
        
        return InsuranceQuoteResponseDTO.PremiumBreakdown.builder()
                .basePremium(breakdown.getBasePremium())
                .ageAdjustment(breakdown.getAgeMultiplier())
                .healthAdjustment(breakdown.getHealthMultiplier())
                .lifestyleAdjustment(breakdown.getLifestyleMultiplier())
                .coverageAdjustment(breakdown.getCoverageMultiplier())
                .finalPremium(breakdown.getFinalPremium())
                .build();
    }
}