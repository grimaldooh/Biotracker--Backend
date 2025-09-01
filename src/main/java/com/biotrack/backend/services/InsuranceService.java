package com.biotrack.backend.services;

import com.biotrack.backend.dto.insurance.InsuranceQuoteRequestDTO;
import com.biotrack.backend.dto.insurance.InsuranceQuoteResponseDTO;
import com.biotrack.backend.dto.insurance.InsuranceQuoteStatsDTO;
import com.biotrack.backend.models.InsuranceQuote;
import com.biotrack.backend.models.enums.QuoteStatus;
import java.util.List;
import java.util.UUID;

public interface InsuranceService {
    
    /**
     * Calcula una nueva cotización de seguro para un paciente
     */
    InsuranceQuoteResponseDTO calculateInsuranceQuote(InsuranceQuoteRequestDTO request);
    
    /**
     * Obtiene todas las cotizaciones de un paciente
     */
    List<InsuranceQuoteResponseDTO> getPatientQuotes(UUID patientId);
    
    /**
     * Obtiene la cotización más reciente activa de un paciente
     */
    InsuranceQuoteResponseDTO getLatestActiveQuote(UUID patientId);
    
    /**
     * Obtiene una cotización específica por ID
     */
    InsuranceQuoteResponseDTO getQuoteById(UUID quoteId);
    
    /**
     * Acepta una cotización (cambiar status)
     */
    InsuranceQuoteResponseDTO acceptQuote(UUID quoteId);
    
    /**
     * Rechaza una cotización (cambiar status)
     */
    InsuranceQuoteResponseDTO rejectQuote(UUID quoteId);
    
    /**
     * Marca cotizaciones expiradas como vencidas
     */
    void markExpiredQuotes();
    
    /**
     * Obtiene estadísticas de cotizaciones
     */
    InsuranceQuoteStatsDTO getQuoteStatistics();
}