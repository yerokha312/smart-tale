package dev.yerokha.smarttale.controller.account;

import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.OrderDto;
import dev.yerokha.smarttale.dto.SmallOrder;
import dev.yerokha.smarttale.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
            summary = "Get my orders", description = "Retrieve all active or completed orders of author. " +
                                                     "Sort by fields of object. Default sorting by deadlineAt",
            tags = {"get", "account", "order"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad param", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            },
            parameters = {
                    @Parameter(name = "q", description = "\"active\" or any other value for completed", required = true),
                    @Parameter(name = "page", description = "Page number"),
                    @Parameter(name = "size", description = "Page size")
            }
    )
    @GetMapping
    public ResponseEntity<CustomPage<SmallOrder>> getOrders(Authentication authentication,
                                                            @RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(advertisementService.getOrders(getUserIdFromAuthToken(authentication), params));
    }

    @Operation(
            summary = "Get order", description = "Retrieve one order by id", tags = {"get", "order", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Order not found", content = @Content)
            }
    )
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDto> getOrder(Authentication authentication, @PathVariable Long orderId) {
        return ResponseEntity.ok(advertisementService.getOrder(getUserIdFromAuthToken(authentication), orderId));
    }

    @Operation(
            summary = "Confirm order", description = "User confirms accepting Org's request", tags = {"post", "order", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order confirmed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "It's not user's order", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Order or org not found", content = @Content),
                    @ApiResponse(responseCode = "410", description = "Link is expired", content = @Content)
            },
            parameters = @Parameter(name = "code", description = "Code for confirming acceptance request", required = true)
    )
    @PostMapping
    public ResponseEntity<String> confirmOrder(Authentication authentication, @RequestParam("code") String code) {
        advertisementService.confirmOrder(code, getUserIdFromAuthToken(authentication));

        return ResponseEntity.ok("Order confirmed");
    }
}
