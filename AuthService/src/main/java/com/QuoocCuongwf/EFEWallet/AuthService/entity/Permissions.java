package com.QuoocCuongwf.EFEWallet.AuthService.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permissions {

    @Id
    private UUID id;

    @Column(unique = true, nullable = false)
    private String name;
}