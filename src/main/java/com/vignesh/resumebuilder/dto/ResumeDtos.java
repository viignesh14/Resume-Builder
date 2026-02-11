package com.vignesh.resumebuilder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

public class ResumeDtos {

    @Data
    public static class ResumeRequestDto {
        @NotBlank
        private String templateType;

        @NotBlank
        private String contentJson;

        private String title;
    }

    @Data
    public static class ResumeResponseDto {
        @NotNull
        private Long id;
        private String templateType;
        private String contentJson;
        private String title;
        private Instant createdAt;
        private Instant updatedAt;
        private boolean isPublic;
    }
}

