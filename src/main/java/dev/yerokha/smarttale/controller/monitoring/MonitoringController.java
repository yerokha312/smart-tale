package dev.yerokha.smarttale.controller.monitoring;

import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.DashboardOrder;
import dev.yerokha.smarttale.dto.MonitoringOrder;
import dev.yerokha.smarttale.dto.OrderSummary;
import dev.yerokha.smarttale.dto.UpdateTaskRequest;
import dev.yerokha.smarttale.service.AdvertisementService;
import dev.yerokha.smarttale.service.OrganizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
            summary = "Update status", tags = {"put", "order", "organization", "monitoring"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Forbidden (not order of organization)", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Not found", content = @Content)
            }
    )
    @PutMapping(value = "/{orderId}", consumes = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasPermission(#orderId, #status, 'UPDATE_STATUS')")
    public ResponseEntity<String> changeStatus(Authentication authentication,
                                               @PathVariable Long orderId,
                                               @RequestBody String status) {
        advertisementService.updateStatus(getUserIdFromAuthToken(authentication), orderId, status);

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
    public ResponseEntity<CustomPage<OrderSummary>> getOrdersHistory(Authentication authentication,
                                                                     @RequestParam(required = false) Map<String, String> params) {

        return ResponseEntity.ok(organizationService.getOrders(
                getUserIdFromAuthToken(authentication),
                params));
    }

    @Operation(
            summary = "Update task", description = "Method for adding, removing employees from task and comment editing. " +
                                                   "Please send only new added or deleted employees' ids in List. Can not add to order with PENDING and COMPLETED status. " +
                                                   "Send empty list if just updating comment",
            tags = {"put", "order", "monitoring", "organization", "employee"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "No permission"),
                    @ApiResponse(responseCode = "404", description = "User, employee, org or task not found"),
            }
    )
    @PutMapping
    @PreAuthorize("hasPermission(#request, 'ASSIGN_EMPLOYEES')")
    public ResponseEntity<String> updateTask(Authentication authentication,
                                             @RequestBody @Valid UpdateTaskRequest request) {

        organizationService.updateTask(getUserIdFromAuthToken(authentication), request);

        return ResponseEntity.ok("Task updated");
    }

    @Operation(
            summary = "Delete task", description = "Deletes task (order) by id if user has permission",
            tags = {"delete", "order", "task", "monitoring", "organization"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "No permission"),
                    @ApiResponse(responseCode = "404", description = "Task not found")
            }
    )
    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasPermission(#authentication, 'DELETE_ORDER')")
    public ResponseEntity<String> deleteTask(Authentication authentication,
                                             @PathVariable Long orderId) {

        advertisementService.deleteTask(getUserIdFromAuthToken(authentication), orderId);

        return ResponseEntity.ok("Task deleted");
    }
}
