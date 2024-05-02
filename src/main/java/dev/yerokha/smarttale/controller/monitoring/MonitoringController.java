package dev.yerokha.smarttale.controller.monitoring;

import dev.yerokha.smarttale.service.AdvertisementService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/monitoring")
public class MonitoringController {

    private final AdvertisementService advertisementService;

    public MonitoringController(AdvertisementService advertisementService) {
        this.advertisementService = advertisementService;
    }

}
