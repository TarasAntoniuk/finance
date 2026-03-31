package com.tarasantoniuk.finance.security.auth.dto;

import com.tarasantoniuk.finance.security.auth.validation.PasswordConstraints;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank
    @Email
    @Schema(description = "User email address", example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    @Pattern(regexp = PasswordConstraints.PATTERN, message = PasswordConstraints.MESSAGE)
    @Schema(description = "User password", example = "SecureP@ss1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
