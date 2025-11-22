package com.tarasantoniuk.finance.core.organization.controller;

import com.tarasantoniuk.finance.core.organization.dto.OrganizationRequestDTO;
import com.tarasantoniuk.finance.core.organization.dto.OrganizationResponseDTO;
import com.tarasantoniuk.finance.core.organization.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@Tag(name = "Core - Organization", description = "Organization management API")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    @Operation(summary = "Get all organizations", description = "Retrieve a list of all organizations")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<OrganizationResponseDTO>> getAllOrganizations() {
        List<OrganizationResponseDTO> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get organization by ID", description = "Retrieve an organization by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization found"),
            @ApiResponse(responseCode = "404", description = "Organization not found")
    })
    public ResponseEntity<OrganizationResponseDTO> getOrganizationById(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long id) {
        OrganizationResponseDTO organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(organization);
    }

    @GetMapping("/country/{countryId}")
    @Operation(summary = "Get organizations by country", description = "Retrieve all organizations in a specific country")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<OrganizationResponseDTO>> getOrganizationsByCountry(
            @Parameter(description = "Country ID", required = true) @PathVariable Long countryId) {
        List<OrganizationResponseDTO> organizations = organizationService.getOrganizationsByCountry(countryId);
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/search")
    @Operation(summary = "Search organizations by name", description = "Search for organizations by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    public ResponseEntity<List<OrganizationResponseDTO>> searchOrganizationsByName(
            @Parameter(description = "Name to search", required = true, example = "Acme")
            @RequestParam String name) {
        List<OrganizationResponseDTO> organizations = organizationService.searchOrganizationsByName(name);
        return ResponseEntity.ok(organizations);
    }

    @PostMapping
    @Operation(summary = "Create a new organization", description = "Create a new organization record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Organization created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<OrganizationResponseDTO> createOrganization(
            @Parameter(description = "Organization data", required = true)
            @Valid @RequestBody OrganizationRequestDTO requestDTO) {
        OrganizationResponseDTO organization = organizationService.createOrganization(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(organization);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an organization", description = "Update an existing organization record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organization updated successfully"),
            @ApiResponse(responseCode = "404", description = "Organization not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<OrganizationResponseDTO> updateOrganization(
            @Parameter(description = "Organization ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated organization data", required = true)
            @Valid @RequestBody OrganizationRequestDTO requestDTO) {
        OrganizationResponseDTO organization = organizationService.updateOrganization(id, requestDTO);
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