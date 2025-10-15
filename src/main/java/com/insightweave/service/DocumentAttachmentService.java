package com.insightweave.service;

import com.insightweave.entity.Document;
import com.insightweave.entity.FileAsset;
import com.insightweave.repository.DocumentRepository;
import com.insightweave.repository.FileAssetRepository;
import com.insightweave.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentAttachmentService {

    private final DocumentRepository docRepo;
    private final FileAssetRepository fileRepo;
    private final StorageService storage;

    @Transactional
    public FileAsset addAttachment(Long docId, MultipartFile file) throws Exception {
        Document doc = docRepo.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + docId));

        var s = storage.save(file);
        var asset = FileAsset.builder()
                .originalFilename(s.original())
                .contentType(s.type() != null ? s.type() : "application/octet-stream")
                .sizeBytes(s.size())
                .storageKey(s.key())
                .sha256(s.sha256())
                .build();

        asset = fileRepo.save(asset);         // persist the row
        doc.getAttachments().add(asset);      // maintain in-memory list
        docRepo.save(doc);                    // keep owning aggregate consistent
        return asset;
    }

    @Transactional
    public void removeAttachment(Long docId, Long fileId) throws Exception {
        var doc = docRepo.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + docId));
        var asset = fileRepo.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found: " + fileId));

        // delete bytes first; ignore if already missing
        try { storage.delete(asset.getStorageKey()); } catch (Exception ignored) {}

        // detach from the collection; orphanRemoval=true will delete the row
        doc.getAttachments().removeIf(a -> a.getId().equals(fileId));
        docRepo.save(doc);
    }
}
