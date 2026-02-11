package com.vignesh.resumebuilder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.List;

public class AtsDtos {

    @Data
    public static class AtsCheckRequestDto {
        @NotNull
        private Long resumeId;
        @NotBlank
        private String jobDescription;
    }

    @Data
    public static class AtsReportResponseDto {
        private Long id;
        private Long resumeId;
        private int score;
        private List<String> matchedKeywords;
        private List<String> missingKeywords;
        private int keywordMatchScore;
        private int formattingScore;
        private int readabilityScore;
        private int sectionCompletenessScore;
        private Instant createdAt;
    }
}

