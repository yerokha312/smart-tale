package dev.yerokha.smarttale.controller;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "My Advertisements", description = "Controller for ads in Private Account")
@RestController
@RequestMapping("/v1/advertisements")
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
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad request param"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @GetMapping
    public ResponseEntity<Page<AdvertisementInterface>> getAds(Authentication authentication,
                                                               @RequestParam(required = false) Map<String, String> params) {

        return ResponseEntity.ok(advertisementService.getAds(getUserIdFromAuthToken(authentication), params));
    }

    @Operation(
            summary = "Get one ad", description = "Returns order or product that belongs to user, distinguish by " +
            "\"orderId\" and \"productId\" field of object",
            tags = {"advertisement", "user", "get", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User or Ad not found")
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
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User or Ad not found")
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

}
