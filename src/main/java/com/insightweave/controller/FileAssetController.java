package com.insightweave.controller;

import com.insightweave.entity.FileAsset;
import com.insightweave.service.FileAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileAssetController {
    private final FileAssetService svc;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileAsset> upload(@RequestPart("file") MultipartFile file) throws Exception {
        if (file.isEmpty()) throw new IllegalArgumentException("Empty file");
        var saved = svc.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved); // includes createdAt/updatedAt via AuditedEntity
    }

    @GetMapping("/{id}")
    public FileAsset info(@PathVariable Long id) { return svc.get(id); }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable Long id) throws Exception {
        var e = svc.get(id);
        var res = svc.download(id);
        String fname = URLEncoder.encode(e.getOriginalFilename(), StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(e.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + fname)
                .contentLength(e.getSizeBytes())
                .body(res);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws Exception {
        svc.delete(id);
    }
}