package com.QuoocsCuongwf.EFEWallet.WalletService.Repository;

import com.QuoocsCuongwf.EFEWallet.WalletService.Enum.WalletStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.WalletEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<WalletEntity, UUID> {
    Boolean existsWalletEntitiesByUserId(UUID walletId,UUID userId);
    Optional<BigDecimal> getBalanceByUserId(UUID userId);
    Optional<WalletEntity> findByUserId(UUID userId);
    boolean existsWalletEntityByUserId(UUID userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT w 
    FROM WalletEntity w 
    WHERE w.userId = :userId
      AND w.walletStatus = :status
""")
    Optional<WalletEntity> findByUserIdForUpdate(
            @Param("userId") UUID userId,
            @Param("status") WalletStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletEntity w WHERE w.walletAddress = :walletAddress AND w.walletStatus = :status")
    Optional<WalletEntity> findByWalletAddressForUpdate(
            String walletAddress,
            WalletStatus status
    );
}
