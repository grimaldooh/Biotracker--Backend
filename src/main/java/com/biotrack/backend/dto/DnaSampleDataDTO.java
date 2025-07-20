package com.biotrack.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DnaSampleDataDTO {
    
    // === CALIDAD DEL DNA ===
    @DecimalMin(value = "0.0", message = "DNA concentration cannot be negative")
    private BigDecimal concentrationNgUl;
    
    @DecimalMin(value = "0.0", message = "Purity ratio cannot be negative")
    private BigDecimal purity260280Ratio;
    
    @DecimalMin(value = "0.0", message = "Purity ratio cannot be negative")
    private BigDecimal purity260230Ratio;
    
    @DecimalMin(value = "0.0", message = "Integrity number cannot be negative")
    private BigDecimal integrityNumber;
    
    // === EXTRACCIÓN ===
    private DnaExtractionMethodDTO extractionMethod;
    private LocalDateTime extractionDate;
    private String extractionTechnician;
    
    // === ALMACENAMIENTO ===
    private String storageBuffer;
    
    @DecimalMin(value = "0.0", message = "Aliquot volume cannot be negative")
    private BigDecimal aliquotVolumeUl;
    
    private String freezerLocation;
    
    // === SECUENCIACIÓN ===
    private SequencingPlatformDTO sequencingPlatform;
    
    @Min(value = 1, message = "Sequencing depth must be at least 1x")
    private Integer sequencingDepth;
    
    private String libraryPrepProtocol;
    
    // === ANÁLISIS GENÓMICO ===
    @Min(value = 0, message = "Total reads cannot be negative")
    private Long totalReads;
    
    @Min(value = 0, message = "Mapped reads cannot be negative")
    private Long mappedReads;
    
    @DecimalMin(value = "0.0", message = "Mapping quality score cannot be negative")
    private BigDecimal mappingQualityScore;
    
    @Min(value = 0, message = "Variants detected cannot be negative")
    private Integer variantsDetected;
    
    @Min(value = 0, message = "SNPs detected cannot be negative")
    private Integer snpsDetected;
    
    @Min(value = 0, message = "Indels detected cannot be negative")
    private Integer indelsDetected;
    
    // === ARCHIVOS DE SECUENCIACIÓN (S3 URLs) ===
    private String fastqR1Url;
    private String fastqR2Url;
    private String vcfFileUrl;
    private String bamFileUrl;
}