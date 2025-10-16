package com.insightweave.controller;

import com.insightweave.dto.*;
import com.insightweave.entity.Document;
import com.insightweave.mapper.DocumentMapper;
import com.insightweave.repository.DocumentRepository;
import com.insightweave.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository repo;
    private final DocumentMapper mapper;
    private final DocumentService documentService;

    public DocumentController(DocumentRepository repo, DocumentMapper mapper, DocumentService documentService) {
        this.repo = repo;
        this.mapper = mapper;
        this.documentService = documentService;
    }

    @PostMapping
    @Operation(summary = "Create a new document")
    public ResponseEntity<DocumentResponse> create(@Valid @RequestBody DocumentCreateRequest req) {
        Document saved = repo.save(mapper.toEntity(req));
        return ResponseEntity.ok(mapper.toResponse(saved));
    }

    @GetMapping
    @Operation(summary = "List documents (paged)")
    public Page<DocumentResponse> all(
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return repo.findAll(pageable).map(mapper::toResponse);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing document")
    public ResponseEntity<DocumentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentCreateRequest req
    ) {
        var existing = repo.findById(id).orElseThrow();
        mapper.updateEntity(existing, req);
        var saved = repo.save(existing);
        return ResponseEntity.ok(mapper.toResponse(saved));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a document (also deletes its files)")
    public void delete(@PathVariable Long id) {
        documentService.deleteWithFiles(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search documents (paged)")
    public Page<DocumentResponse> search(
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return repo.search(q, pageable).map(mapper::toResponse);
    }
}
