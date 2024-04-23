package dev.yerokha.smarttale.controller.account;

import dev.yerokha.smarttale.dto.FullPurchase;
import dev.yerokha.smarttale.dto.Purchase;
import dev.yerokha.smarttale.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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

@Tag(name = "Purchase", description = "EPs for My purchases")
@RestController
@RequestMapping("/v1/account/purchases")
public class PurchaseController {

    private final AdvertisementService advertisementService;

    public PurchaseController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Operation(
            summary = "All purchases", description = "It's not clear what PO wants, made for purchased products(equipments)",
            tags = {"purchase", "user", "get", "account"},
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
    public ResponseEntity<Page<Purchase>> getPurchases(Authentication authentication,
                                                       @RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(advertisementService.getPurchases(getUserIdFromAuthToken(authentication), params));
    }

    @Operation(
            summary = "One purchase", description = "Get one purchase by unique id", tags = {"purchase", "user", "get", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Purchase not found", content = @Content),
            }
    )
    @GetMapping("/{productId}")
    public ResponseEntity<FullPurchase> getPurchase(Authentication authentication,
                                                    @PathVariable Long productId) {
        return ResponseEntity.ok(advertisementService.getPurchase(getUserIdFromAuthToken(authentication), productId));
    }
}