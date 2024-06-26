package dev.yerokha.smarttale.controller.account;

import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.Purchase;
import dev.yerokha.smarttale.dto.PurchaseSummary;
import dev.yerokha.smarttale.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "Purchases", description = "EPs for My purchases")
@RestController
@RequestMapping("/v1/account/purchases")
public class PurchaseController {

    private final AdvertisementService advertisementService;

    public PurchaseController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Operation(
            summary = "All purchases", description = "Returns purchases with product in it",
            tags = {"purchase", "user", "get", "account", "product"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
            },
            parameters = {
                    @Parameter(name = "page", description = "Page number, default 0"),
                    @Parameter(name = "size", description = "Page size, default 8")
            }

    )
    @GetMapping
    public ResponseEntity<CustomPage<PurchaseSummary>> getPurchases(Authentication authentication,
                                                                    @RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(advertisementService.getPurchases(getUserIdFromAuthToken(authentication), params));
    }

    @Operation(
            summary = "One purchase", description = "Returns purchase with details and product in it",
            tags = {"purchase", "user", "get", "account", "product"},
            responses = {

                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(
                            schema = @Schema(implementation = Purchase.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Purchase not found", content = @Content),
            }
    )
    @GetMapping("/{productId}")
    public ResponseEntity<Purchase> getPurchase(@PathVariable Long productId, Authentication authentication) {
        return ResponseEntity.ok(advertisementService.getPurchase(productId, getUserIdFromAuthToken(authentication)));
    }
}
