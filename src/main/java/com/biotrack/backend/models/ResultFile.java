package com.biotrack.backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "result_files")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultFile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(name = "file_name", nullable = false)
    private String fileName;
    
    @Column(name = "s3_key", nullable = false, unique = true)
    private String s3Key;
    
    @Column(name = "s3url", nullable = false)
    private String s3Url;
    
    @Column(name = "file_size")
    private Long fileSize;
    
    @Column(name = "content_type")
    private String contentType;
    
    @Builder.Default
    @Column(name = "processing_status")
    private String processingStatus = "PENDING";
    
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;
}
