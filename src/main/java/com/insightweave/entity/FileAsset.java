// entity/FileAsset.java
package com.insightweave.entity;

import com.insightweave.common.AuditedEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "file_assets")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FileAsset extends AuditedEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String originalFilename;
    @Column(nullable = false) private String contentType;
    @Column(nullable = false) private Long sizeBytes;

    @Column(nullable = false, unique = true) // e.g., UUID.ext or cloud key
    private String storageKey;

    @Column(length = 64, nullable = false)   // SHA-256 hex
    private String sha256;
}
