package com.vignesh.resumebuilder.repository;

import com.vignesh.resumebuilder.entity.ResumeEntity;
import com.vignesh.resumebuilder.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<ResumeEntity, Long> {
    List<ResumeEntity> findByUserAndDeletedFalseOrderByUpdatedAtDesc(UserEntity user);
    Optional<ResumeEntity> findByIdAndDeletedFalse(Long id);
    Optional<ResumeEntity> findByIdAndDeletedFalseAndIsPublicTrue(Long id);
}

