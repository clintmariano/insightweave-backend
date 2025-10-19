// entity/Summary.java
package com.insightweave.entity;

import com.insightweave.common.AuditedEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "summaries", indexes = {
    @Index(name = "idx_summaries_file_asset_id", columnList = "file_asset_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Summary extends AuditedEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_asset_id", nullable = false)
    private Long fileAssetId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summaryText;

    @Column(length = 100)
    private String modelName;

    @Column(length = 50)
    private String style = "concise";

    @Column(name = "latency_ms")
    private Integer latencyMs;
}
