package com.QuoocCuongwf.EFEWallet.AuthService.repository;

import com.QuoocCuongwf.EFEWallet.AuthService.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RolesReponsitory extends JpaRepository<Roles, UUID> {
    public Optional<Roles> findRolesByName(String name);
    public Optional<Roles> findRolesById(UUID id);
}
