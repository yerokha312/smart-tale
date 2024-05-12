package dev.yerokha.smarttale.controller.monitoring;

import dev.yerokha.smarttale.dto.AssignmentRequest;
import dev.yerokha.smarttale.dto.DashboardOrder;
import dev.yerokha.smarttale.dto.MonitoringOrder;
import dev.yerokha.smarttale.dto.OrderSummary;
import dev.yerokha.smarttale.service.AdvertisementService;
import dev.yerokha.smarttale.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "Monitoring")
@RestController
@RequestMapping("/v1/monitoring")
public class MonitoringController {

    private final AdvertisementService advertisementService;
    private final OrganizationService organizationService;

    public MonitoringController(AdvertisementService advertisementService, OrganizationService organizationService) {
        this.advertisementService = advertisementService;
        this.organizationService = organizationService;
    }

    @Operation(
            summary = "Get dashboard", tags = {"get", "order", "organization", "monitoring"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @GetMapping
    public ResponseEntity<List<DashboardOrder>> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(advertisementService.getDashboard(getUserIdFromAuthToken(authentication)));
    }

    @Operation(
            summary = "Get order", tags = {"get", "order", "organization", "monitoring"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden (not order of organization)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @GetMapping("/{orderId}")
    public ResponseEntity<MonitoringOrder> getOrder(Authentication authentication, @PathVariable Long orderId) {
        return ResponseEntity.ok(advertisementService.getMonitoringOrder(getUserIdFromAuthToken(authentication), orderId));
    }

    @Operation(
            summary = "Change status", tags = {"put", "order", "organization", "monitoring"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden (not order of organization)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @PutMapping(value = "/{orderId}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> changeStatus(Authentication authentication,
                                               @PathVariable Long orderId,
                                               @RequestBody String status) {
        advertisementService.changeStatus(getUserIdFromAuthToken(authentication), orderId, status);

        return ResponseEntity.ok("Status changed");
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
                    @Parameter(name = "dateFrom", description = "If dateType is not null, then dateFrom is required"),
                    @Parameter(name = "dateTo", description = "If dateType is not null, then dateTo is required"),
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
    public ResponseEntity<Page<OrderSummary>> getOrdersHistory(Authentication authentication,
                                                               @RequestParam Map<String, String> params) {

        return ResponseEntity.ok(organizationService.getOrders(
                getUserIdFromAuthToken(authentication),
                params));
    }

    @PutMapping
    @PreAuthorize("hasPermission(request, 'ASSIGN_EMPLOYEES')")
    public ResponseEntity<String> assignEmployeesToTask(Authentication authentication,
                                                        @RequestBody @Valid AssignmentRequest request) {

        organizationService.assignEmployees(getUserIdFromAuthToken(authentication), request);

        return ResponseEntity.ok("Employees assigned");
    }


}
