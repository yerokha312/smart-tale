package dev.yerokha.smarttale.controller.account;

import dev.yerokha.smarttale.dto.CurrentOrder;
import dev.yerokha.smarttale.dto.Employee;
import dev.yerokha.smarttale.service.OrganizationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@RestController
@RequestMapping("/v1/account/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/orders")
    public ResponseEntity<Page<CurrentOrder>> getOrders(Authentication authentication,
                                                        @RequestParam Map<String, String> params) {
        return ResponseEntity.ok(organizationService.getOrders(getUserIdFromAuthToken(authentication), params));
    }

    @GetMapping("/employees")
    public ResponseEntity<Page<Employee>> getEmployees(Authentication authentication,
                                                       @RequestParam Map<String, String> params) {
        return ResponseEntity.ok(organizationService.getEmployees(getUserIdFromAuthToken(authentication), params));
    }
}
