package dev.yerokha.smarttale.controller.account;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.FullOrder;
import dev.yerokha.smarttale.dto.FullProduct;
import dev.yerokha.smarttale.dto.Order;
import dev.yerokha.smarttale.dto.Product;
import dev.yerokha.smarttale.dto.UpdateAdRequest;
import dev.yerokha.smarttale.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "My Advertisements", description = "Controller for ads in Private Account")
@RestController
@RequestMapping("/v1/account/advertisements")
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    public AdvertisementController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Operation(
            summary = "Get all ads", description = "Returns orders and products that belong to user, distinguish by " +
                                                   "\"orderId\" and \"productId\" field of objects",
            tags = {"advertisement", "user", "get", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(
                            anyOf = {Order.class, Product.class}))),
                    @ApiResponse(responseCode = "400", description = "Bad request param", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            },
            parameters = {
                    @Parameter(name = "page", description = "Page number, default 0"),
                    @Parameter(name = "size", description = "Page size, default 10"),
                    @Parameter(name = "q", description = "Query orders or products", examples = {
                            @ExampleObject(name = "q", value = "\"products\" or \"orders\""),
                            @ExampleObject(name = "q", value = "orders")
                    })
            }
    )
    @GetMapping
    public ResponseEntity<Page<AdvertisementInterface>> getAds(Authentication authentication,
                                                               @RequestParam(required = false) Map<String, String> params) {

        return ResponseEntity.ok(advertisementService.getAds(getUserIdFromAuthToken(authentication), params));
    }

    @Operation(
            summary = "Get one ad", description = "Returns status or product that belongs to user, distinguish by " +
                                                  "\"orderId\" and \"productId\" field of object",
            tags = {"advertisement", "user", "get", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(
                            anyOf = {FullOrder.class, FullProduct.class}))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or Ad not found", content = @Content)
            }
    )
    @GetMapping("/{advertisementId}")
    public ResponseEntity<AdvertisementInterface> getAd(Authentication authentication,
                                                        @PathVariable Long advertisementId) {

        return ResponseEntity.ok(advertisementService.getAd(getUserIdFromAuthToken(authentication),
                advertisementId));
    }

    @Operation(
            summary = "Action on ad", description = "EP for close(1)/disclose(2)/delete(3)/restore(4) an ad",
            tags = {"advertisement", "user", "delete", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or Ad not found", content = @Content)
            }

    )
    @DeleteMapping("/{advertisementId}/{actionId}")
    public ResponseEntity<String> interactWithAd(Authentication authentication,
                                                 @PathVariable Long advertisementId,
                                                 @PathVariable byte actionId) {

        return ResponseEntity.ok(advertisementService.interactWithAd(getUserIdFromAuthToken(authentication),
                advertisementId,
                actionId));
    }

    @Operation(
            summary = "Update ad", description = "EP for updating an advertisement. Send date as string format: yyyy-MM-dd",
            tags = {"advertisement", "user", "put", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User or Ad not found", content = @Content)
            }
    )
    @PutMapping
    public ResponseEntity<String> updateAd(Authentication authentication,
                                           @RequestPart("dto") @Valid UpdateAdRequest request,
                                           @RequestPart(value = "images", required = false) List<MultipartFile> files) {

        if (files != null && !files.isEmpty()) {
            if (files.size() > 5) {
                throw new IllegalArgumentException("You can not upload more than 5 images");
            }
            for (MultipartFile file : files) {
                AccountController.validateImage(file);
            }
        }

        advertisementService.updateAd(getUserIdFromAuthToken(authentication), request, files);
        return ResponseEntity.ok("Advertisement updated successfully!");
    }
}
