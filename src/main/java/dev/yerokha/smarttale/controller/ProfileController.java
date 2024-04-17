package dev.yerokha.smarttale.controller;

import dev.yerokha.smarttale.dto.Profile;
import dev.yerokha.smarttale.dto.UpdateProfileRequest;
import dev.yerokha.smarttale.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@Tag(name = "Account", description = "Controller for personal account")
@RestController
@RequestMapping("/v1/account")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get own profile", description = "Returns basic information about the user and avatar",
            tags = {"account", "get", "user", "profile"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Profile not found"),
            }
    )
    @GetMapping
    public ResponseEntity<Profile> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getProfileByUserId(getUserIdFromAuthToken(authentication)));
    }

    @Operation(
            summary = "Update own profile", description = "Requires all fields filled, returns Profile object",
            tags = {"account", "put", "user", "profile"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Bad request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "Profile not found"),
                    @ApiResponse(responseCode = "409", description = "Email or phone already exists")
            }
    )
    @PutMapping
    public ResponseEntity<Profile> updateProfile(Authentication authentication,
                                                 @RequestBody UpdateProfileRequest request) {

        if (!request.isValid()) {
            throw new IllegalArgumentException("Name fields should be either all Latin or all Cyrillic");
        }

        return ResponseEntity.ok(userService.updateProfile(getUserIdFromAuthToken(authentication), request));
    }
}
