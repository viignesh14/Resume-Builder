package com.vignesh.resumebuilder.controller;

import com.vignesh.resumebuilder.dto.ContactDtos;
import com.vignesh.resumebuilder.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody ContactDtos.ContactRequestDto dto) {
        contactService.createMessage(dto);
        return ResponseEntity.ok().build();
    }
}

