package com.QuoocsCuongwf.EFEWallet.WalletService.security;

import com.QuoocsCuongwf.EFEWallet.WalletService.Repository.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class OwnershipService {

    @Autowired
    private WalletRepository walletRepository;

    public boolean isOwner(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());

        return walletRepository.findByUserId(userId)
                .map(wallet -> wallet.getUserId().equals(userId))
                .orElse(false);
    }
}