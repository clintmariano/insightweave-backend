package com.insightweave.service;

import com.insightweave.entity.Document;
import com.insightweave.repository.DocumentRepository;
import com.insightweave.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository repo;
    private final StorageService storage;

    @Transactional
    public void deleteWithFiles(Long id) {
        Document doc = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found: " + id));

        // remove bytes on disk for each attachment (ignore if already missing)
        doc.getAttachments().forEach(a -> {
            try {
                storage.delete(a.getStorageKey());
            } catch (Exception ignored) {
            }
        });

        // DB rows are removed thanks to orphanRemoval=true
        repo.delete(doc);
    }
}
