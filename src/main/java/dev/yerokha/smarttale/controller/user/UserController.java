package dev.yerokha.smarttale.controller.user;

import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.InviteUserRequest;
import dev.yerokha.smarttale.dto.UserDto;
import dev.yerokha.smarttale.dto.UserSummary;
import dev.yerokha.smarttale.service.OrganizationService;
import dev.yerokha.smarttale.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;
    private final OrganizationService organizationService;

    public UserController(UserService userService, OrganizationService organizationService) {
        this.userService = userService;
        this.organizationService = organizationService;
    }

    @Operation(
            summary = "Get all users", tags = {"user", "get"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
            },
            parameters = {
                    @Parameter(name = "page", description = "Page number", example = "1"),
                    @Parameter(name = "size", description = "Page size", example = "1")
            }
    )
    @GetMapping
    public ResponseEntity<CustomPage<UserSummary>> getUsers(@RequestParam(required = false) Map<String, String> params) {
        return ResponseEntity.ok(userService.getUsers(params));
    }

    @Operation(
            summary = "Get one user", tags = {"user", "get"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            }
    )
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getOneUser(@PathVariable Long userId, Authentication authentication) {
        return ResponseEntity.ok(userService.getOneUser(userId, authentication));
    }

    @Operation(
            summary = "Invite a user", tags = {"user", "post"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "403", description = "No permission or role", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
            }
    )
    @PostMapping("/invite")
    @PreAuthorize("hasPermission('user', 'INVITE_EMPLOYEE')")
    public ResponseEntity<String> inviteEmployee(Authentication authentication,
                                                 @RequestBody @Valid InviteUserRequest request) {

        organizationService.inviteEmployeeByUserId(getUserIdFromAuthToken(authentication), request);

        return new ResponseEntity<>("Invitation sent", HttpStatus.CREATED);
    }
}
