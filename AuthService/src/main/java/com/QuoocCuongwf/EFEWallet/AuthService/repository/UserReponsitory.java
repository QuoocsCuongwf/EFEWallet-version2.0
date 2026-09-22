package com.QuoocCuongwf.EFEWallet.AuthService.repository;

import com.QuoocCuongwf.EFEWallet.AuthService.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserReponsitory extends JpaRepository<User, UUID> {
     public Optional<User> findUserByEmail(String email);

     boolean existsByEmail(String email);
     @Override
     Optional<User> findById(UUID uuid);
}
