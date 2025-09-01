package com.biotrack.backend.repositories;

import com.biotrack.backend.models.InsuranceQuote;
import com.biotrack.backend.models.enums.QuoteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InsuranceQuoteRepository extends JpaRepository<InsuranceQuote, UUID> {
    
    List<InsuranceQuote> findByPatientIdOrderByQuoteDateDesc(UUID patientId);
    
    List<InsuranceQuote> findByPatientIdAndStatusOrderByQuoteDateDesc(UUID patientId, QuoteStatus status);
    
    Optional<InsuranceQuote> findTopByPatientIdAndStatusOrderByQuoteDateDesc(UUID patientId, QuoteStatus status);
    
    @Query("SELECT iq FROM InsuranceQuote iq WHERE iq.validUntil < :currentDate AND iq.status = :status")
    List<InsuranceQuote> findExpiredQuotes(@Param("currentDate") LocalDateTime currentDate, 
                                          @Param("status") QuoteStatus status);
    
    @Query("SELECT COUNT(iq) FROM InsuranceQuote iq WHERE iq.quoteDate >= :startDate")
    Long countQuotesSince(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT AVG(iq.monthlyPremium) FROM InsuranceQuote iq WHERE iq.quoteDate >= :startDate")
    Double getAveragePremiumSince(@Param("startDate") LocalDateTime startDate);

    // Agregar estos métodos al repository existente:
    Long countByStatusAndQuoteDateAfter(QuoteStatus status, LocalDateTime date);

    @Query("SELECT AVG(iq.riskScore) FROM InsuranceQuote iq WHERE iq.quoteDate >= :startDate")
    Double getAverageRiskScoreSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT iq.coverageType, COUNT(iq), AVG(iq.monthlyPremium), AVG(iq.riskScore) " +
           "FROM InsuranceQuote iq WHERE iq.quoteDate >= :startDate GROUP BY iq.coverageType")
    List<Object[]> getCoverageTypeStatsSince(@Param("startDate") LocalDateTime startDate);
}