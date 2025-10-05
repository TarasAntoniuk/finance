package com.tarasantoniuk.finance.organization.controller;

import com.tarasantoniuk.finance.organization.dto.OrganizationRequestDTO;
import com.tarasantoniuk.finance.organization.dto.OrganizationResponseDTO;
import com.tarasantoniuk.finance.organization.service.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public ResponseEntity<List<OrganizationResponseDTO>> getAllOrganizations() {
        List<OrganizationResponseDTO> organizations = organizationService.getAllOrganizations();
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> getOrganizationById(@PathVariable Long id) {
        OrganizationResponseDTO organization = organizationService.getOrganizationById(id);
        return ResponseEntity.ok(organization);
    }

    @GetMapping("/country/{countryId}")
    public ResponseEntity<List<OrganizationResponseDTO>> getOrganizationsByCountry(
            @PathVariable Long countryId) {
        List<OrganizationResponseDTO> organizations = organizationService.getOrganizationsByCountry(countryId);
        return ResponseEntity.ok(organizations);
    }

    @GetMapping("/search")
    public ResponseEntity<List<OrganizationResponseDTO>> searchOrganizationsByName(
            @RequestParam String name) {
        List<OrganizationResponseDTO> organizations = organizationService.searchOrganizationsByName(name);
        return ResponseEntity.ok(organizations);
    }

    @PostMapping
    public ResponseEntity<OrganizationResponseDTO> createOrganization(
            @Valid @RequestBody OrganizationRequestDTO requestDTO) {
        OrganizationResponseDTO organization = organizationService.createOrganization(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(organization);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponseDTO> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody OrganizationRequestDTO requestDTO) {
        OrganizationResponseDTO organization = organizationService.updateOrganization(id, requestDTO);
        return ResponseEntity.ok(organization);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
}