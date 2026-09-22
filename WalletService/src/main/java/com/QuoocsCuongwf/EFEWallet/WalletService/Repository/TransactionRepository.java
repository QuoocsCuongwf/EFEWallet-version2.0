package com.QuoocsCuongwf.EFEWallet.WalletService.Repository;

import com.QuoocsCuongwf.EFEWallet.WalletService.entity.TransactionEntity;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {

    @Query("""
            SELECT t FROM TransactionEntity t
            WHERE (t.fromUserId = :userId OR t.toUserId = :userId)
              AND (:status IS NULL OR t.status = :status)
              AND (:type IS NULL OR t.type = :type)
            """)
    Page<TransactionEntity> findHistoryByUserId(
            @Param("userId") UUID userId,
            @Param("status") TransactionStatus status,
            @Param("type") TransactionType type,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM TransactionEntity t
            WHERE t.id = :id
              AND (t.fromUserId = :userId OR t.toUserId = :userId)
            """)
    Optional<TransactionEntity> findByIdAndUserId(
            @Param("id") UUID id,
            @Param("userId") UUID userId
    );
}
