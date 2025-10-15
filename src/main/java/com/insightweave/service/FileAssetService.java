package com.insightweave.service;

import com.insightweave.entity.FileAsset;
import com.insightweave.repository.FileAssetRepository;
import com.insightweave.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service @RequiredArgsConstructor
public class FileAssetService {
    private final StorageService storage;
    private final FileAssetRepository repo;

    public FileAsset upload(MultipartFile file) throws Exception {
        var s = storage.save(file);
        var entity = FileAsset.builder()
                .originalFilename(s.original())
                .contentType(s.type() != null ? s.type() : "application/octet-stream")
                .sizeBytes(s.size())
                .storageKey(s.key())
                .sha256(s.sha256())
                .build();
        return repo.save(entity);
    }
    public FileAsset get(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new ResponseStatusException(NOT_FOUND, "File not found: " + id));
    }
    public Resource download(Long id) throws Exception { return storage.loadAsResource(get(id).getStorageKey()); }
    public void delete(Long id) throws Exception { var e = get(id); storage.delete(e.getStorageKey()); repo.delete(e); }
}
