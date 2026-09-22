package com.QuoocsCuongwf.EFEWallet.WalletService.entity;

import com.QuoocsCuongwf.EFEWallet.WalletService.enums.IdempotencyStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_keys",
        uniqueConstraints = @UniqueConstraint(columnNames = {"idempotencyKey", "userId"}),
        indexes = {
                @Index(name = "idx_idem_user", columnList = "userId"),
                @Index(name = "idx_idem_key", columnList = "idempotencyKey")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKey {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String idempotencyKey;

    private String requestHash;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    @Lob
    private String responseBody; // JSON

    private Integer responseStatus;

    @Enumerated(EnumType.STRING)
    private TransactionType resourceType;

    private String resourceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime expiredAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.expiredAt = createdAt.plusMinutes(10); // TTL
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}