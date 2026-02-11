package com.vignesh.resumebuilder.controller;

import com.vignesh.resumebuilder.dto.AtsDtos;
import com.vignesh.resumebuilder.service.AtsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ats")
public class AtsController {

    private final AtsService atsService;

    public AtsController(AtsService atsService) {
        this.atsService = atsService;
    }

    @PostMapping("/check")
    public ResponseEntity<AtsDtos.AtsReportResponseDto> check(
            HttpServletRequest request,
            @Valid @RequestBody AtsDtos.AtsCheckRequestDto dto
    ) {
        Long userId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(atsService.runCheck(userId, dto));
    }

    @GetMapping("/history/{userId}")
    public ResponseEntity<List<AtsDtos.AtsReportResponseDto>> history(
            HttpServletRequest request,
            @PathVariable Long userId
    ) {
        Long actualUserId = (Long) request.getAttribute("userId");
        return ResponseEntity.ok(atsService.getHistory(actualUserId));
    }
}

