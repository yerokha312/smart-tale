package dev.yerokha.smarttale.controller.publicEndpoints;

import dev.yerokha.smarttale.dto.Organization;
import dev.yerokha.smarttale.dto.OrganizationSummary;
import dev.yerokha.smarttale.service.OrganizationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/organizations")
public class OrganizationsController {

    private final OrganizationService organizationService;

    public OrganizationsController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    public ResponseEntity<Page<OrganizationSummary>> getAllOrganizations(@RequestParam Map<String, String> params) {
        return ResponseEntity.ok(organizationService.getAllOrganizations(params));
    }

    @GetMapping("/{organizationId}")
    public ResponseEntity<Organization> getOrganizationById(@PathVariable Long organizationId) {
        return ResponseEntity.ok(organizationService.getOrganizationById(organizationId));
    }

}
