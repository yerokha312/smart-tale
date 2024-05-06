package dev.yerokha.smarttale.controller.monitoring;

import dev.yerokha.smarttale.dto.DashboardOrder;
import dev.yerokha.smarttale.dto.MonitoringOrder;
import dev.yerokha.smarttale.service.AdvertisementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "Monitoring")
@RestController
@RequestMapping("/v1/monitoring")
public class MonitoringController {

    private final AdvertisementService advertisementService;

    public MonitoringController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
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

    @GetMapping("/{orderId}")
    public ResponseEntity<MonitoringOrder> getOrder(Authentication authentication, @PathVariable Long orderId) {
        return ResponseEntity.ok(advertisementService.getMonitoringOrder(getUserIdFromAuthToken(authentication), orderId));
    }

    @PutMapping(value = "/{orderId}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> changeStatus(Authentication authentication,
                                               @PathVariable Long orderId,
                                               @RequestBody String status) {
        advertisementService.changeStatus(getUserIdFromAuthToken(authentication), orderId, status);

        return ResponseEntity.ok("Status changed");
    }
//
//    @GetMapping("/history")


}
