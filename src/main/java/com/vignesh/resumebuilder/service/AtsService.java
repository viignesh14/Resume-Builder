package com.vignesh.resumebuilder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vignesh.resumebuilder.dto.AtsDtos;
import com.vignesh.resumebuilder.entity.AtsReportEntity;
import com.vignesh.resumebuilder.entity.ResumeEntity;
import com.vignesh.resumebuilder.entity.UserEntity;
import com.vignesh.resumebuilder.repository.AtsReportRepository;
import com.vignesh.resumebuilder.repository.ResumeRepository;
import com.vignesh.resumebuilder.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AtsService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final AtsReportRepository atsReportRepository;
    private final ObjectMapper objectMapper;

    public AtsService(ResumeRepository resumeRepository,
                      UserRepository userRepository,
                      AtsReportRepository atsReportRepository,
                      ObjectMapper objectMapper) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.atsReportRepository = atsReportRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AtsDtos.AtsReportResponseDto runCheck(Long userId, AtsDtos.AtsCheckRequestDto dto) {
        ResumeEntity resume = resumeRepository.findByIdAndDeletedFalse(dto.getResumeId())
                .orElseThrow(() -> new NoSuchElementException("Resume not found"));
        if (!resume.getUser().getId().equals(userId)) {
            throw new SecurityException("Forbidden");
        }

        String resumeText = extractResumeText(resume.getResumeData());
        String jdText = dto.getJobDescription();

        Set<String> jdKeywords = extractKeywords(jdText);
        Set<String> resumeKeywords = extractKeywords(resumeText);

        List<String> matched = jdKeywords.stream()
                .filter(resumeKeywords::contains)
                .sorted()
                .toList();

        List<String> missing = jdKeywords.stream()
                .filter(k -> !resumeKeywords.contains(k))
                .sorted()
                .toList();

        int keywordMatchScore = jdKeywords.isEmpty()
                ? 0
                : (int) Math.round((matched.size() * 100.0) / jdKeywords.size());

        int formattingScore = computeFormattingScore(resume);
        int readabilityScore = computeReadabilityScore(resumeText);
        int sectionScore = computeSectionCompletenessScore(resume.getResumeData());

        int overallScore = (int) Math.round(
                keywordMatchScore * 0.45 +
                        formattingScore * 0.2 +
                        readabilityScore * 0.15 +
                        sectionScore * 0.2
        );

        AtsReportEntity report = new AtsReportEntity();
        report.setResume(resume);
        report.setJobDescription(jdText);
        report.setScore(overallScore);
        report.setKeywordMatchScore(keywordMatchScore);
        report.setFormattingScore(formattingScore);
        report.setReadabilityScore(readabilityScore);
        report.setSectionCompletenessScore(sectionScore);
        report.setCreatedAt(Instant.now());
        try {
            report.setMatchedKeywords(objectMapper.writeValueAsString(matched));
            report.setMissingKeywords(objectMapper.writeValueAsString(missing));
        } catch (JsonProcessingException e) {
            report.setMatchedKeywords("[]");
            report.setMissingKeywords("[]");
        }

        AtsReportEntity saved = atsReportRepository.save(report);
        return toDto(saved, matched, missing);
    }

    @Transactional(readOnly = true)
    public List<AtsDtos.AtsReportResponseDto> getHistory(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        List<ResumeEntity> resumes = resumeRepository.findByUserAndDeletedFalseOrderByUpdatedAtDesc(user);
        List<AtsReportEntity> reports = atsReportRepository.findByResumeInOrderByCreatedAtDesc(resumes);
        return reports.stream()
                .map(this::toDtoFromEntityOnly)
                .collect(Collectors.toList());
    }

    private AtsDtos.AtsReportResponseDto toDto(AtsReportEntity entity, List<String> matched, List<String> missing) {
        AtsDtos.AtsReportResponseDto dto = new AtsDtos.AtsReportResponseDto();
        dto.setId(entity.getId());
        dto.setResumeId(entity.getResume().getId());
        dto.setScore(entity.getScore());
        dto.setMatchedKeywords(matched);
        dto.setMissingKeywords(missing);
        dto.setKeywordMatchScore(entity.getKeywordMatchScore());
        dto.setFormattingScore(entity.getFormattingScore());
        dto.setReadabilityScore(entity.getReadabilityScore());
        dto.setSectionCompletenessScore(entity.getSectionCompletenessScore());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    private AtsDtos.AtsReportResponseDto toDtoFromEntityOnly(AtsReportEntity entity) {
        List<String> matched = Collections.emptyList();
        List<String> missing = Collections.emptyList();
        try {
            matched = objectMapper.readValue(entity.getMatchedKeywords(), List.class);
            missing = objectMapper.readValue(entity.getMissingKeywords(), List.class);
        } catch (Exception ignored) {
        }
        return toDto(entity, matched, missing);
    }

    private String extractResumeText(String resumeJson) {
        if (resumeJson == null || resumeJson.isBlank()) {
            return "";
        }
        try {
            Map<String, Object> map = objectMapper.readValue(resumeJson, Map.class);
            StringBuilder sb = new StringBuilder();
            collectText(map, sb);
            return sb.toString();
        } catch (Exception e) {
            return resumeJson;
        }
    }

    @SuppressWarnings("unchecked")
    private void collectText(Object node, StringBuilder sb) {
        if (node == null) return;
        if (node instanceof Map<?, ?> m) {
            m.values().forEach(v -> collectText(v, sb));
        } else if (node instanceof Collection<?> c) {
            c.forEach(v -> collectText(v, sb));
        } else {
            sb.append(' ').append(node.toString());
        }
    }

    private Set<String> extractKeywords(String text) {
        if (text == null) return Collections.emptySet();
        String[] tokens = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+.# ]", " ")
                .split("\\s+");
        Set<String> stopwords = Set.of(
                "and", "or", "the", "a", "an", "to", "of", "in", "on", "for", "with",
                "is", "are", "as", "at", "by", "this", "that", "from", "be", "have",
                "has", "will", "can", "should"
        );
        return Arrays.stream(tokens)
                .filter(t -> t.length() > 2)
                .filter(t -> !stopwords.contains(t))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private int computeFormattingScore(ResumeEntity resume) {
        String template = resume.getTemplate() == null ? "" : resume.getTemplate().toLowerCase(Locale.ROOT);
        // Favor ATS-friendly template
        if (template.contains("ats")) {
            return 95;
        }
        if (template.contains("modern") || template.contains("professional") || template.contains("minimal")) {
            return 80;
        }
        return 70;
    }

    private int computeReadabilityScore(String text) {
        if (text == null || text.isBlank()) return 0;
        int length = text.length();
        if (length < 800) return 60;
        if (length < 2000) return 85;
        if (length < 4000) return 90;
        return 75;
    }

    private int computeSectionCompletenessScore(String resumeJson) {
        if (resumeJson == null || resumeJson.isBlank()) return 0;
        try {
            Map<String, Object> map = objectMapper.readValue(resumeJson, Map.class);
            int total = 0;
            int present = 0;
            String[] keys = {"personalInfo", "summary", "experience", "projects", "skills", "education"};
            for (String key : keys) {
                total++;
                Object v = map.get(key);
                if (v != null && !v.toString().isBlank()) {
                    present++;
                }
            }
            return (int) Math.round((present * 100.0) / total);
        } catch (Exception e) {
            return 60;
        }
    }
}

