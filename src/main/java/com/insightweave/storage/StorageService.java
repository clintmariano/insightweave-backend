package com.insightweave.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    record Stored(String key, String sha256, long size, String type, String original) {}
    Stored save(MultipartFile file) throws Exception;
    Resource loadAsResource(String key) throws Exception;
    boolean delete(String key) throws Exception;
}
