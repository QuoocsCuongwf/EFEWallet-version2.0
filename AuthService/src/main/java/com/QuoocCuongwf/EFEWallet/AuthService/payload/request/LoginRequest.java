package com.QuoocCuongwf.EFEWallet.AuthService.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank
    @NotBlank(message = "Password cannot be empty")
    private String password;
}
