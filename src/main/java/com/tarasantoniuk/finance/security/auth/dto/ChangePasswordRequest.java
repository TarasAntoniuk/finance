package com.tarasantoniuk.finance.security.auth.dto;

import com.tarasantoniuk.finance.security.auth.validation.PasswordConstraints;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank
    @Schema(description = "Current password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currentPassword;

    @NotBlank
    @Size(min = 8, max = 100)
    @Pattern(regexp = PasswordConstraints.PATTERN, message = PasswordConstraints.MESSAGE)
    @Schema(description = "New password", example = "NewSecureP@ss1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
