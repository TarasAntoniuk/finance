package com.tarasantoniuk.finance.security.user.dto;

import com.tarasantoniuk.finance.security.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary view of a user for admin listings")
public class UserSummaryDto {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User email address", example = "user@example.com")
    private String email;

    @Schema(description = "User role", example = "USER")
    private UserRole role;

    @Schema(description = "Whether the account is active", example = "true")
    private Boolean isActive;

    public UserSummaryDto() {
    }

    public UserSummaryDto(Long id, String email, UserRole role, Boolean isActive) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
