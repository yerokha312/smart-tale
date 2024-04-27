package dev.yerokha.smarttale.controller.account;

import dev.yerokha.smarttale.dto.OrderDto;
import dev.yerokha.smarttale.dto.SmallOrder;
import dev.yerokha.smarttale.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "My Orders", description = "EPs for retrieving active/completed orders")
@RestController
@RequestMapping("/v1/account/orders")
public class OrderController {

    private final AdvertisementService advertisementService;

    public OrderController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Operation(
            summary = "Get orders", description = "Retrieve all active or completed orders",
            tags = {"get", "account", "status"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad param"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            },
            parameters = {
                    @Parameter(name = "q", description = "\"active\" or any other value for completed", required = true),
                    @Parameter(name = "page", description = "Page number"),
                    @Parameter(name = "size", description = "Page size")
            }
    )
    @GetMapping
    public ResponseEntity<Page<SmallOrder>> getOrders(Authentication authentication,
                                                      @RequestParam Map<String, String> params) {
        return ResponseEntity.ok(advertisementService.getOrders(getUserIdFromAuthToken(authentication), params));
    }

    @Operation(
            summary = "Get status", description = "Retrieve one status by id", tags = {"get", "status", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Order not found")
            }
    )
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(Authentication authentication, @PathVariable Long orderId) {
        return ResponseEntity.ok(advertisementService.getOrder(getUserIdFromAuthToken(authentication), orderId));
    }
}
