package dev.yerokha.smarttale.controller.organization;

import dev.yerokha.smarttale.dto.Employee;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.dto.OrderSummary;
import dev.yerokha.smarttale.dto.Organization;
import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "Organization", description = "Organization controller EPs")
@RestController
@RequestMapping("/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @Operation(
            summary = "Get own Org", tags = {"get", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Org not found")
            }
    )
    @GetMapping
    public ResponseEntity<Organization> getOrganization(Authentication authentication) {
        return ResponseEntity.ok(organizationService.getOrganization(getUserIdFromAuthToken(authentication)));
    }


    @Operation(
            summary = "Get order history", description = "Get all orders of organization",
            tags = {"organization", "get", "order", "monitoring"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad param request", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or organization not found", content = @Content)
            },
            parameters = {
                    @Parameter(name = "active", description = "true, null or false"),
                    @Parameter(name = "dateType", description = "accepted, deadline, completed"),
                    @Parameter(name = "startDate"),
                    @Parameter(name = "endDate"),
                    @Parameter(name = "date", description = "Exact date without date range"),
                    @Parameter(name = "page", description = "Page number. Default 0"),
                    @Parameter(name = "size", description = "Page size. Default 6"),
                    @Parameter(name = "[sort]", description = "Sorting property. Equals to object field. Can be multiple" +
                            "sorting properties. Default \"acceptedAt\"",
                            examples = {
                                    @ExampleObject(name = "deadlineAt", value = "asc", description = "\"sorting param=asc/desc\"")
                            })
            }
    )
    @GetMapping("/orders")
    public ResponseEntity<Page<OrderSummary>> getOrders(Authentication authentication,
                                                        @RequestParam Map<String, String> params) {
        return ResponseEntity.ok(organizationService.getOrders(getUserIdFromAuthToken(authentication), params));
    }

    @Operation(
            summary = "Get employees", description = "Get all employees and invitees of organization",
            tags = {"organization", "get", "employee", "user", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad param request", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or organization not found", content = @Content)
            },
            parameters = {
                    @Parameter(name = "page", description = "Page number. Default 0"),
                    @Parameter(name = "size", description = "Page size. Default 6"),
                    @Parameter(name = "name", description = "Sorts by name. name=asc/desc"),
                    @Parameter(name = "orders", description = "Sorts by active orders number"),
                    @Parameter(name = "status", description = "Not a property, needs front implementation"),
                    @Parameter(name = "[sort]", description = "Sorting property. Equals to object field. Can be multiple" +
                            "sorting properties. Default \"name\""
                    )
            }
    )
    @GetMapping("/employees")
    public ResponseEntity<Page<Employee>> getEmployees(Authentication authentication,
                                                       @RequestParam Map<String, String> params) {
        return ResponseEntity.ok(organizationService.getEmployees(
                getUserIdFromAuthToken(authentication),
                params));
    }

    @Operation(
            summary = "Positions", description = "Get all positions of organization. Drop down request",
            tags = {"organization", "get", "position", "user", "account", "employee"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or organization not found", content = @Content)
            }
    )
    @GetMapping("/positions")
    public ResponseEntity<List<Position>> getPositions(Authentication authentication) {
        return ResponseEntity.ok(organizationService.getPositions(getUserIdFromAuthToken(authentication)));
    }

    @Operation(
            summary = "Invite employee", description = "Sends email to invited person's address",
            tags = {"post", "account", "user", "employee", "organization"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Invite successful"),
                    @ApiResponse(responseCode = "400", description = "Bad request. Email is validated"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Forbidden"),
                    @ApiResponse(responseCode = "404", description = "User, org or position not found")
            }
    )
    @PostMapping("/employees")
    public ResponseEntity<String> inviteEmployee(Authentication authentication, @RequestBody @Valid InviteRequest request) {
        String email = organizationService.inviteEmployee(getUserIdFromAuthToken(authentication), request);

        return new ResponseEntity<>(String.format("Invite sent to %s", email), HttpStatus.CREATED);
    }
}
