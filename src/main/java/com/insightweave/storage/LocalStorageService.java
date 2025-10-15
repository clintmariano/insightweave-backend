package com.insightweave.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.*;
import java.security.*;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {
    @Value("${insightweave.upload-dir:uploads}") String uploadDir;
    private Path root;

    @PostConstruct void init() throws Exception {
        root = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(root);
    }

    @Override public Stored save(MultipartFile file) throws Exception {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String key = UUID.randomUUID() + (ext != null ? "." + ext : "");
        Path target = root.resolve(key);

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream in = file.getInputStream()) {
            // compute hash while copying
            byte[] buf = new byte[8192]; int r;
            try (var out = Files.newOutputStream(target)) {
                while ((r = in.read(buf)) != -1) { md.update(buf, 0, r); out.write(buf, 0, r); }
            }
        }
        String digest = HexFormat.of().formatHex(md.digest());
        return new Stored(key, digest, file.getSize(), file.getContentType(), file.getOriginalFilename());
    }

    @Override public Resource loadAsResource(String key) throws Exception {
        Path p = root.resolve(key).normalize();
        if (!Files.exists(p)) throw new NoSuchFileException(key);
        return new FileSystemResource(p);
    }

    @Override public boolean delete(String key) throws Exception {
        return Files.deleteIfExists(root.resolve(key).normalize());
    }
}
