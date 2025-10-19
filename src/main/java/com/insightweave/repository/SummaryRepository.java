package com.insightweave.repository;

import com.insightweave.entity.Summary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SummaryRepository extends JpaRepository<Summary, Long> {
    
    List<Summary> findByFileAssetId(Long fileAssetId);
    
    Optional<Summary> findFirstByFileAssetIdOrderByCreatedAtDesc(Long fileAssetId);
    
    void deleteByFileAssetId(Long fileAssetId);
}
