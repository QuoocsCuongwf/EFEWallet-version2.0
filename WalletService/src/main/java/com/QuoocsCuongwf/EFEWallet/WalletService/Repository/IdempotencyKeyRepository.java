package com.QuoocsCuongwf.EFEWallet.WalletService.Repository;

import com.QuoocsCuongwf.EFEWallet.WalletService.entity.IdempotencyKey;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.enums.TransactionType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {

    Optional<IdempotencyKey> findByIdempotencyKeyAndUserId(String idempotencyKey, String userId);

    boolean existsByIdempotencyKeyAndUserId(String idempotencyKey, String userId);

    Optional<IdempotencyKey> findByResourceIdAndResourceType(String resourceId, TransactionType resourceType);

    List<IdempotencyKey> findByStatus(TransactionStatus status);

    void deleteByCreatedAtBefore(LocalDateTime time);

    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM IdempotencyKey i WHERE i.idempotencyKey = :key AND i.userId = :userId")
    Optional<IdempotencyKey> findForUpdate(String key, String userId);
}
