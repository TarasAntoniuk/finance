package com.tarasantoniuk.finance.security.user.controller;

import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.security.user.dto.UserDetailDto;
import com.tarasantoniuk.finance.security.user.dto.UserSummaryDto;
import com.tarasantoniuk.finance.security.user.enums.UserRole;
import com.tarasantoniuk.finance.security.user.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin - User Management", description = "Admin endpoints for user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final UserManagementService userManagementService;

    public AdminUserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @Operation(summary = "List all users (paginated)")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    public ResponseEntity<PageResponse<UserSummaryDto>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(userManagementService.listUsers(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user details")
    @ApiResponse(responseCode = "200", description = "User retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<UserDetailDto> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userManagementService.getUser(id));
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Change user role")
    @ApiResponse(responseCode = "204", description = "Role changed successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> changeRole(@PathVariable Long id,
                                           @RequestParam UserRole role) {
        userManagementService.changeRole(id, role);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Activate or deactivate user")
    @ApiResponse(responseCode = "204", description = "Status changed successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized (requires ADMIN role)")
    @ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<Void> changeStatus(@PathVariable Long id,
                                             @RequestParam boolean active) {
        userManagementService.setActive(id, active);
        return ResponseEntity.noContent().build();
    }
}
