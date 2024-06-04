package dev.yerokha.smarttale.controller.market;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.Card;
import dev.yerokha.smarttale.dto.CreateAdInterface;
import dev.yerokha.smarttale.dto.CreateJobRequest;
import dev.yerokha.smarttale.dto.CreateOrderRequest;
import dev.yerokha.smarttale.dto.CreateProductRequest;
import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.FullOrderCard;
import dev.yerokha.smarttale.dto.FullProductCard;
import dev.yerokha.smarttale.service.AdvertisementService;
import dev.yerokha.smarttale.util.Authorities;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getUserAuthoritiesFromToken;
import static dev.yerokha.smarttale.util.ImageValidator.validateImage;

@Tag(name = "Marketplace", description = "EPs for marketplace")
@RestController
@RequestMapping("/v1/market")
public class MarketplaceController {

    private final AdvertisementService advertisementService;

    public MarketplaceController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Operation(
            summary = "Get market ads", description = "Get orders, products or jobs by mandatory \"type\" param",
            tags = {"get", "order", "product", "jobs", "market", "advertisement"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad param", content = @Content)
            },
            parameters = {
                    @Parameter(name = "type", required = true, description = "products or orders"),
                    @Parameter(name = "page", description = "default 0"),
                    @Parameter(name = "size", description = "default 10")
            }
    )
    @GetMapping
    public ResponseEntity<CustomPage<Card>> getAds(@RequestParam(required = false) Map<String, String> params,
                                                   Authentication authentication) {

        return ResponseEntity.ok(advertisementService.getMarketAds(params, authentication));
    }

    @Operation(
            summary = "Get market ad", description = "Get order, product or job by id",
            tags = {"get", "order", "product", "job", "market", "advertisement"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success", content = @Content(schema = @Schema(
                            anyOf = {FullProductCard.class, FullOrderCard.class}))),
                    @ApiResponse(responseCode = "404", description = "Ad not found", content = @Content)
            }
    )
    @GetMapping("/{advertisementId}")
    public ResponseEntity<AdvertisementInterface> getAd(@PathVariable Long advertisementId, Authentication authentication) {
        advertisementService.incrementViewCount(advertisementId);

        return ResponseEntity.ok(advertisementService.getMarketAd(advertisementId, authentication)); //TODO personalize for requested user
    }

    @Operation(
            summary = "Handle advertisement", description = "Purchase product, accept order or apply job",
            tags = {"post", "market", "advertisement", "product", "order", "job"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not found"),
                    @ApiResponse(responseCode = "410", description = "Already purchased"),
            }
    )
    @PostMapping("/{advertisementId}")
    public ResponseEntity<String> handleAdvertisementAction(@PathVariable Long advertisementId,
                                                            Authentication authentication) {
        return ResponseEntity.ok(advertisementService.handleAdvertisement(advertisementId, authentication));
    }

    @Operation(
            summary = "Place advertisement", description = "Create Order or Product. Send date as string format: yyyy-MM-dd",
            tags = {"post", "market", "order", "product"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Ad created"),
                    @ApiResponse(responseCode = "400", description = "Bad request", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Employee does not have permission to create job ad", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            },
            requestBody = @RequestBody(content = @Content(schema = @Schema(anyOf = {
                    CreateProductRequest.class, CreateOrderRequest.class, CreateJobRequest.class
            })))
    )
    @PostMapping
    public ResponseEntity<String> placeAdvertisement(@RequestPart("dto") @Valid CreateAdInterface request,
                                                     @RequestPart(value = "images", required = false) List<MultipartFile> files,
                                                     Authentication authentication) {

        if (files != null && !files.isEmpty()) {
            if (files.size() > 5) {
                throw new IllegalArgumentException("You can not upload more than 5 images");
            }
            for (MultipartFile file : files) {
                validateImage(file);
            }
        }

        if (request instanceof CreateJobRequest) {
            if (!hasInviteEmployeePermission(authentication)) {
                return new ResponseEntity<>("User does not have permission to create job advertisements", HttpStatus.FORBIDDEN);
            }
        }

        return new ResponseEntity<>(advertisementService.createAd(
                request,
                files,
                authentication), HttpStatus.CREATED);
    }

    private boolean hasInviteEmployeePermission(Authentication authentication) {
        int userAuthorities = getUserAuthoritiesFromToken(authentication);
        return (userAuthorities & Authorities.INVITE_EMPLOYEE.getBitmask()) > 0;
    }
}
