package com.vignesh.resumebuilder.service;

import com.vignesh.resumebuilder.dto.ResumeDtos;
import com.vignesh.resumebuilder.entity.ResumeEntity;
import com.vignesh.resumebuilder.entity.UserEntity;
import com.vignesh.resumebuilder.repository.ResumeRepository;
import com.vignesh.resumebuilder.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    public ResumeService(ResumeRepository resumeRepository, UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResumeDtos.ResumeResponseDto createResume(Long userId, ResumeDtos.ResumeRequestDto dto) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        ResumeEntity entity = new ResumeEntity();
        entity.setUser(user);
        entity.setTemplate(dto.getTemplateType());
        entity.setResumeData(dto.getContentJson());
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity.setDeleted(false);

        ResumeEntity saved = resumeRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public ResumeDtos.ResumeResponseDto updateResume(Long userId, Long resumeId, ResumeDtos.ResumeRequestDto dto) {
        ResumeEntity entity = getOwnedResume(userId, resumeId);
        entity.setTemplate(dto.getTemplateType());
        entity.setResumeData(dto.getContentJson());
        entity.setUpdatedAt(Instant.now());
        return toDto(resumeRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ResumeDtos.ResumeResponseDto getResume(Long userId, Long resumeId) {
        ResumeEntity entity = getOwnedResume(userId, resumeId);
        return toDto(entity);
    }

    @Transactional(readOnly = true)
    public List<ResumeDtos.ResumeResponseDto> getUserResumes(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return resumeRepository.findByUserAndDeletedFalseOrderByUpdatedAtDesc(user).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        ResumeEntity entity = getOwnedResume(userId, resumeId);
        entity.setDeleted(true);
        entity.setUpdatedAt(Instant.now());
        resumeRepository.save(entity);
    }

    @Transactional
    public ResumeDtos.ResumeResponseDto duplicateResume(Long userId, Long resumeId) {
        ResumeEntity original = getOwnedResume(userId, resumeId);
        ResumeEntity copy = new ResumeEntity();
        copy.setUser(original.getUser());
        copy.setTemplate(original.getTemplate());
        copy.setResumeData(original.getResumeData());
        copy.setCreatedAt(Instant.now());
        copy.setUpdatedAt(Instant.now());
        copy.setDeleted(false);
        copy.setPublic(false);
        ResumeEntity saved = resumeRepository.save(copy);
        return toDto(saved);
    }

    @Transactional
    public ResumeDtos.ResumeResponseDto setPublic(Long userId, Long resumeId, boolean isPublic) {
        ResumeEntity entity = getOwnedResume(userId, resumeId);
        entity.setPublic(isPublic);
        entity.setUpdatedAt(Instant.now());
        return toDto(resumeRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ResumeDtos.ResumeResponseDto getPublicResume(Long resumeId) {
        ResumeEntity entity = resumeRepository.findByIdAndDeletedFalseAndIsPublicTrue(resumeId)
                .orElseThrow(() -> new NoSuchElementException("Public resume not found"));
        return toDto(entity);
    }

    private ResumeEntity getOwnedResume(Long userId, Long resumeId) {
        ResumeEntity entity = resumeRepository.findByIdAndDeletedFalse(resumeId)
                .orElseThrow(() -> new NoSuchElementException("Resume not found"));
        if (!entity.getUser().getId().equals(userId)) {
            throw new SecurityException("Forbidden");
        }
        return entity;
    }

    private ResumeDtos.ResumeResponseDto toDto(ResumeEntity entity) {
        ResumeDtos.ResumeResponseDto dto = new ResumeDtos.ResumeResponseDto();
        dto.setId(entity.getId());
        dto.setTemplateType(entity.getTemplate());
        dto.setContentJson(entity.getResumeData());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setPublic(entity.isPublic());
        return dto;
    }
}

