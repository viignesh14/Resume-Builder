package com.vignesh.resumebuilder.service;

import com.vignesh.resumebuilder.dto.ContactDtos;
import com.vignesh.resumebuilder.entity.ContactMessageEntity;
import com.vignesh.resumebuilder.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ContactService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    public void createMessage(ContactDtos.ContactRequestDto dto) {
        ContactMessageEntity entity = new ContactMessageEntity();
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setMessage(dto.getMessage());
        entity.setCreatedAt(Instant.now());
        entity.setHandled(false);
        contactMessageRepository.save(entity);
    }
}

