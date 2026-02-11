package com.vignesh.resumebuilder.repository;

import com.vignesh.resumebuilder.entity.ContactMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessageEntity, Long> {
}

