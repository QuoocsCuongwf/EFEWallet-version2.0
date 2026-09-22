package com.QuoocsCuongwf.EFEWallet.WalletService.service;

import com.QuoocsCuongwf.EFEWallet.WalletService.Enum.WalletStatus;
import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.WalletRepository;
import com.QuoocsCuongwf.EFEWallet.WalletService.entity.WalletEntity;
import com.QuoocsCuongwf.EFEWallet.WalletService.exception.WalletIsExistedException;
import com.QuoocsCuongwf.EFEWallet.WalletService.exception.WalletNotFoundException;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.BalanceResponse;
import com.QuoocsCuongwf.EFEWallet.WalletService.payload.response.WalletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
@AllArgsConstructor
@Slf4j
public class WalletService {
    private final WalletRepository walletRepository;

    private final RedisTemplate<Object, Object> redisTemplate;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    public BalanceResponse balance (UUID userId){
        WalletEntity wallet = walletRepository.findByUserId(userId)
                .orElseThrow();
        BigDecimal balance = wallet.getBalance();
        log.info("Balance of user {} equal {} ", userId,balance);
        BalanceResponse balanceResponse=BalanceResponse.builder()
                .balance(balance)
                .time(LocalDateTime.now())
                .build();
        return balanceResponse;
    }

    @Transactional
    public boolean debit(BigDecimal amount, UUID userId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        WalletEntity wallet = walletRepository.findByUserIdForUpdate(userId,WalletStatus.ACTIVATE)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        if (wallet.getBalance().compareTo(amount) < 0) {
            return false;
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);
        return true;
    }
    @Transactional
    public boolean credit(BigDecimal amount, String walletAddress) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }
        WalletEntity wallet = walletRepository.findByWalletAddressForUpdate(walletAddress,WalletStatus.ACTIVATE)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
        return true;
    }


    public WalletResponse wallet (UUID userId){
        WalletEntity walletEntity=walletRepository.findByUserId(userId)
                .orElseThrow(()-> new WalletNotFoundException("Wallet " + userId + " not found"));
        return WalletResponse.builder()
                .walletId(walletEntity.getId())
                .walletAddress(walletEntity.getWalletAddress())
                .userId(walletEntity.getUserId())
                .balance(walletEntity.getBalance())
                .status(walletEntity.getWalletStatus())
                .createAt(walletEntity.getCreatedAt())
                .build();
    }
    @Transactional
    public WalletResponse generation (UUID userId) {
        WalletEntity existing = walletRepository.findByUserId(userId).orElse(null);

        if (existing != null) {
            return WalletResponse.builder()
                    .walletId(existing.getId())
                    .walletAddress(existing.getWalletAddress())
                    .status(existing.getWalletStatus())
                    .build();
        }
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        new SecureRandom().nextBytes(bytes);

        StringBuilder sb = new StringBuilder("0x");
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        String address = sb.toString();

        WalletEntity wallet = WalletEntity.builder()
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .walletAddress(address)
                .walletStatus(WalletStatus.ACTIVATE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        walletRepository.save(wallet);

        return WalletResponse.builder()
                .walletId(wallet.getId())
                .walletAddress(wallet.getWalletAddress())
                .status(wallet.getWalletStatus())
                .build();
    }

}
