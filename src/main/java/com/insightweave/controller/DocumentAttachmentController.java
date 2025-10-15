// src/main/java/com/insightweave/controller/DocumentAttachmentController.java
package com.insightweave.controller;

import com.insightweave.dto.FileAssetDto;
import com.insightweave.mapper.FileAssetMapper;
import com.insightweave.repository.DocumentRepository;
import com.insightweave.service.DocumentAttachmentService;
import com.insightweave.service.FileAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/documents/{docId}/attachments")
@RequiredArgsConstructor
public class DocumentAttachmentController {

    private final DocumentRepository docRepo;
    private final DocumentAttachmentService svc;
    private final FileAssetService fileSvc;
    private final FileAssetMapper fileMapper;            // ✅ add

    private static final Set<String> ALLOWED = Set.of(
            "application/pdf","text/plain","image/png","image/jpeg"
    );

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileAssetDto> uploadToDocument(
            @PathVariable Long docId,
            @RequestPart("file") MultipartFile file
    ) throws Exception {
        var doc = docRepo.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Document not found: " + docId));

        if (file.isEmpty()) throw new ResponseStatusException(BAD_REQUEST, "Empty file");
        var ct = file.getContentType();
        if (ct == null || !ALLOWED.contains(ct)) {
            throw new ResponseStatusException(BAD_REQUEST, "Unsupported content type: " + ct);
        }

        var saved = svc.addAttachment(doc.getId(), file);
        return ResponseEntity.status(HttpStatus.CREATED).body(fileMapper.toDto(saved));   // ✅ DTO
    }

    @GetMapping
    public List<FileAssetDto> list(@PathVariable Long docId) {
        var doc = docRepo.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Document not found: " + docId));
        return fileMapper.toDtoList(doc.getAttachments());                                 // ✅ DTOs
    }

    @DeleteMapping("/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long docId, @PathVariable Long fileId) throws Exception {
        var doc = docRepo.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Document not found: " + docId));

        boolean belongs = doc.getAttachments().stream().anyMatch(a -> a.getId().equals(fileId));
        if (!belongs) {
            throw new ResponseStatusException(NOT_FOUND,
                    "File " + fileId + " does not belong to document " + docId);
        }
        svc.removeAttachment(docId, fileId);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> download(
            @PathVariable Long docId, @PathVariable Long fileId
    ) throws Exception {
        var doc = docRepo.findById(docId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Document not found: " + docId));

        boolean match = doc.getAttachments().stream().anyMatch(a -> a.getId().equals(fileId));
        if (!match) {
            throw new ResponseStatusException(NOT_FOUND,
                    "File " + fileId + " does not belong to document " + docId);
        }

        var asset = fileSvc.get(fileId);
        var resource = fileSvc.download(fileId);

        String encoded = URLEncoder.encode(asset.getOriginalFilename(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(asset.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .contentLength(asset.getSizeBytes())
                .body(resource);
    }
}
