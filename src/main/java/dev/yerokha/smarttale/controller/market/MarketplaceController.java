package dev.yerokha.smarttale.controller.market;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.Card;
import dev.yerokha.smarttale.dto.FullOrderCard;
import dev.yerokha.smarttale.dto.FullProductCard;
import dev.yerokha.smarttale.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "Marketplace", description = "EPs for marketplace")
@RestController
@RequestMapping("/v1/market")
public class MarketplaceController {

    private final AdvertisementService advertisementService;

    public MarketplaceController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

    @Operation(
            summary = "Get ads", description = "Get orders and products by mandatory \"type\" param",
            tags = {"get", "order", "product", "market", "advertisement"},
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
    public ResponseEntity<Page<Card>> getAds(@RequestParam Map<String, String> params) {

        return ResponseEntity.ok(advertisementService.getAds(params));
    }

    @Operation(
            summary = "Get one ad", description = "Get order or product by id",
            tags = {"get", "order", "product", "market", "advertisement"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = FullOrderCard.class))),
                    @ApiResponse(responseCode = "200", description = "Success",
                            content = @Content(schema = @Schema(implementation = FullProductCard.class))),
                    @ApiResponse(responseCode = "404", description = "Ad not found", content = @Content)
            }
    )
    @GetMapping("/{advertisementId}")
    public ResponseEntity<AdvertisementInterface> getAd(@PathVariable Long advertisementId) {
        return ResponseEntity.ok(advertisementService.getAd(advertisementId));
    }

    @Operation(
            summary = "Purchase product", description = "Buy a product by ad id", tags = {"post", "market", "product"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not found"),
                    @ApiResponse(responseCode = "410", description = "Already purchased"),
            }
    )
    @PostMapping("/{advertisementId}")
    public ResponseEntity<String> purchase(@PathVariable Long advertisementId,
                                           Authentication authentication) {

        advertisementService.purchaseProduct(advertisementId, getUserIdFromAuthToken(authentication));

        return ResponseEntity.ok("Purchase success");
    }

    @Operation(
            summary = "Accept order", description = "Accept order by it's id", tags = {"put", "market", "order"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Not found"),
                    @ApiResponse(responseCode = "410", description = "Already accepted"),
            }
    )
    @PutMapping("/{advertisementId}")
    public ResponseEntity<String> accept(@PathVariable Long advertisementId,
                                         Authentication authentication) {

        advertisementService.acceptOrder(advertisementId, getUserIdFromAuthToken(authentication));

        return ResponseEntity.ok("Order accepted");
    }
}
