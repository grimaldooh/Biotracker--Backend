package com.biotrack.backend.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "result_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String fileName;

    private String s3Url;

    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "sample_id")
    private Sample sample;
}
