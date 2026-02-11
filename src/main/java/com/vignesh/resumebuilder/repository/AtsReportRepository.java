package com.vignesh.resumebuilder.repository;

import com.vignesh.resumebuilder.entity.AtsReportEntity;
import com.vignesh.resumebuilder.entity.ResumeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AtsReportRepository extends JpaRepository<AtsReportEntity, Long> {
    List<AtsReportEntity> findByResumeInOrderByCreatedAtDesc(List<ResumeEntity> resumes);
}

