package com.insightweave.controller;

import com.insightweave.dto.*;
import com.insightweave.entity.Document;
import com.insightweave.mapper.DocumentMapper;
import com.insightweave.repository.DocumentRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentRepository repo;
    private final DocumentMapper mapper;

    public DocumentController(DocumentRepository repo, DocumentMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<DocumentResponse> create(@Valid @RequestBody DocumentCreateRequest req) {
        Document saved = repo.save(mapper.toEntity(req));
        return ResponseEntity.ok(mapper.toResponse(saved));
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> all() {
        var list = repo.findAll().stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/{id}")
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
