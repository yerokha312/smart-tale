package dev.yerokha.smarttale.controller.publicEndpoints;

import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.Organization;
import dev.yerokha.smarttale.dto.OrganizationSummary;
import dev.yerokha.smarttale.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

    @Operation(
            summary = "Get all organizations", description = "Public endpoint", tags = {"get", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Organizations paged list"),
            },
            parameters = {
                    @Parameter(name = "page", description = "default 0"),
                    @Parameter(name = "size", description = "default 10")
            }
    )
    @GetMapping
    public ResponseEntity<CustomPage<OrganizationSummary>> getAllOrganizations(@RequestParam(required = false)
                                                                               Map<String, String> params) {
        return ResponseEntity.ok(organizationService.getAllOrganizations(params));
    }

    @Operation(
            summary = "Get organization by id", description = "Public endpoint", tags = {"get", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @GetMapping("/{organizationId}")
    public ResponseEntity<Organization> getOrganizationById(@PathVariable Long organizationId) {
        return ResponseEntity.ok(organizationService.getOrganizationById(organizationId));
    }

}
