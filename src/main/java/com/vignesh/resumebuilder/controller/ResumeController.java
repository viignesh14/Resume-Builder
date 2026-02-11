package com.vignesh.resumebuilder.controller;

import com.vignesh.resumebuilder.dto.ResumeDtos;
import com.vignesh.resumebuilder.service.ResumeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public ResponseEntity<ResumeDtos.ResumeResponseDto> create(
            HttpServletRequest request,
            @Valid @RequestBody ResumeDtos.ResumeRequestDto dto
    ) {
        Long userId = (Long) request.getAttribute("userId");
        ResumeDtos.ResumeResponseDto created = resumeService.createResume(userId, dto);
        return ResponseEntity.created(URI.create("/api/resume/" + created.getId())).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeDtos.ResumeResponseDto> getById(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(resumeService.getResume(userId, id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResumeDtos.ResumeResponseDto> update(
            HttpServletRequest request,
            @PathVariable Long id,
            @Valid @RequestBody ResumeDtos.ResumeRequestDto dto
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(resumeService.updateResume(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        Long userId = (Long) request.getAttribute("userId");
        resumeService.deleteResume(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResumeDtos.ResumeResponseDto>> getByUser(
            HttpServletRequest request,
            @PathVariable Long userId
    ) {
        // For safety, ignore path userId and use token's userId
        Long actualUserId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(resumeService.getUserResumes(actualUserId));
    }

    @PostMapping("/{id}/duplicate")
    public ResponseEntity<ResumeDtos.ResumeResponseDto> duplicate(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(resumeService.duplicateResume(userId, id));
    }

    @PutMapping("/{id}/public")
    public ResponseEntity<ResumeDtos.ResumeResponseDto> setPublic(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam("value") boolean value
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(resumeService.setPublic(userId, id, value));
    }

    @GetMapping("/public/{id}")
    public ResponseEntity<ResumeDtos.ResumeResponseDto> getPublic(@PathVariable Long id) {
        return ResponseEntity.ok(resumeService.getPublicResume(id));
    }
}

