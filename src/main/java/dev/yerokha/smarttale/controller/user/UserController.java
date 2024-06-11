package dev.yerokha.smarttale.controller.user;

import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.UserDto;
import dev.yerokha.smarttale.dto.UserSummary;
import dev.yerokha.smarttale.service.OrganizationService;
import dev.yerokha.smarttale.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;
    private final OrganizationService organizationService;

    public UserController(UserService userService, OrganizationService organizationService) {
        this.userService = userService;
        this.organizationService = organizationService;
    }

    @GetMapping
    public ResponseEntity<CustomPage<UserSummary>> getUsers(@RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(userService.getUsers(params));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getOneUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getOneUser(userId));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<String> inviteEmployee(Authentication authentication,
                                                 @PathVariable Long userId) {
        return null;
    }
}
