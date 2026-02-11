package com.vignesh.resumebuilder.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ats_reports")
@Getter
@Setter
public class AtsReportEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resume_id")
    private ResumeEntity resume;

    @Lob
    @Column(name = "job_description", columnDefinition = "LONGTEXT")
    private String jobDescription;

    private int score;

    @Lob
    @Column(name = "matched_keywords", columnDefinition = "LONGTEXT")
    private String matchedKeywords; // JSON array as string

    @Lob
    @Column(name = "missing_keywords", columnDefinition = "LONGTEXT")
    private String missingKeywords; // JSON array as string

    private int keywordMatchScore;
    private int formattingScore;
    private int readabilityScore;
    private int sectionCompletenessScore;

    @Column(nullable = false)
    private Instant createdAt;
}

