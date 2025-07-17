package com.biotrack.backend.models;

import com.biotrack.backend.models.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;
    
    @Column(name = "s3_key", nullable = true)
    private String s3Key;
    
    @Column(name = "s3url") // Nota: usar el mismo nombre que tienes en otras tablas
    private String s3Url;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
    
    @Column(name = "openai_model")
    private String openaiModel;
    
    @Column(name = "token_usage")
    private Integer tokenUsage;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;
}