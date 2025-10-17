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
    private final TextExtractionService textExtraction;

    @Transactional
    public FileAsset addAttachment(Long docId, MultipartFile file) throws Exception {
        Document doc = docRepo.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + docId));

        var s = storage.save(file);

        // Extract text if the file type is supported
        String extractedText = "";
        String contentType = s.type() != null ? s.type() : "application/octet-stream";
        if (textExtraction.isTextExtractable(contentType)) {
            try {
                extractedText = textExtraction.extractText(file.getInputStream(), s.original());
            } catch (Exception e) {
                // Log but don't fail the upload if text extraction fails
                // The TextExtractionService already logs the error
            }
        }

        var asset = FileAsset.builder()
                .originalFilename(s.original())
                .contentType(contentType)
                .sizeBytes(s.size())
                .storageKey(s.key())
                .sha256(s.sha256())
                .extractedText(extractedText.isEmpty() ? null : extractedText)
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
