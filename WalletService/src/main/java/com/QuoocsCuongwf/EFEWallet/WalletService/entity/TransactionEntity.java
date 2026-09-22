package com.QuoocsCuongwf.EFEWallet.WalletService.entity;

import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionType;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "transactions",
        indexes = {
                @Index(name = "idx_from_user", columnList = "fromUserId"),
                @Index(name = "idx_to_user", columnList = "toUserId"),
                @Index(name = "idx_external_tx", columnList = "externalTransactionId"),
                @Index(name = "idx_idempotency", columnList = "idempotencyKey")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID fromUserId;

    @Column(nullable = false)
    private String fromWallet;

    @Column(nullable = false)
    private UUID toUserId;

    @Column(nullable = false)
    private String toWallet;

    @Column(nullable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(precision = 19, scale = 4)
    private BigDecimal fee = BigDecimal.ZERO;

    private String currency;

    @Column(length = 500)
    private String description;

    private String externalTransactionId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}