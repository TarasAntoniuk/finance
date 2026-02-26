package com.tarasantoniuk.finance.core.organization.controller;

import com.tarasantoniuk.finance.common.dto.PageResponse;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationRequestDto;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDto;
import com.tarasantoniuk.finance.core.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@Validated
@Tag(name = "Core - Organization", description = "Organization management API")
public class OrganizationController {

    private static final int MAX_PAGE_SIZE = 500;

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    @Operation(summary = "Get all organizations", description = "Retrieve a paginated list of all organizations")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list")
    public ResponseEntity<PageResponse<OrganizationResponseDto>> getAllOrganizations(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page (max 500)", example = "50")
            @RequestParam(defaultValue = "50") @Min(1) int size) {
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        PageResponse<OrganizationResponseDto> response = organizationService.getAllOrganizations(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID", description = "Retrieve an organization by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization found"),
            @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<OrganizationResponseDto> getOrganizationById(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long id) {
        OrganizationResponseDto organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(organization);
    }

    @GetMapping("/country/{countryId}")
    @Operation(summary = "Get organizations by country", description = "Retrieve all organizations in a specific country")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<OrganizationResponseDto>> getOrganizationsByCountry(
            @Parameter(description = "Country ID", required = true) @PathVariable Long countryId,
            @Parameter(description = "Maximum number of results (max 500)", example = "500")
            @RequestParam(defaultValue = "500") @Min(1) int limit) {
        if (limit > MAX_PAGE_SIZE) {
            limit = MAX_PAGE_SIZE;
        }
        List<OrganizationResponseDto> organizations = organizationService.getOrganizationsByCountry(countryId, limit);
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/search")
    @Operation(summary = "Search organizations by name", description = "Search for organizations by name (case-insensitive, paginated)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated list")
    public ResponseEntity<PageResponse<OrganizationResponseDto>> searchOrganizationsByName(
            @Parameter(description = "Name to search", required = true, example = "Acme")
            @RequestParam String name,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of items per page (max 500)", example = "50")
            @RequestParam(defaultValue = "50") @Min(1) int size) {
        if (size > MAX_PAGE_SIZE) {
            size = MAX_PAGE_SIZE;
        }
        PageResponse<OrganizationResponseDto> response = organizationService.searchOrganizationsByName(name, page, size);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Create a new organization", description = "Create a new organization record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organization created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<OrganizationResponseDto> createOrganization(
            @Parameter(description = "Organization data", required = true)
            @Valid @RequestBody OrganizationRequestDto requestDTO) {
        OrganizationResponseDto organization = organizationService.createOrganization(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(organization);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an organization", description = "Update an existing organization record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization updated successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<OrganizationResponseDto> updateOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated organization data", required = true)
            @Valid @RequestBody OrganizationRequestDto requestDTO) {
        OrganizationResponseDto organization = organizationService.updateOrganization(id, requestDTO);
        return ResponseEntity.ok(organization);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an organization", description = "Delete an organization by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Organization deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<Void> deleteOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
}