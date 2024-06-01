package dev.yerokha.smarttale.controller.account;

import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.Invitation;
import dev.yerokha.smarttale.dto.Profile;
import dev.yerokha.smarttale.dto.UpdateProfileRequest;
import dev.yerokha.smarttale.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;
import static dev.yerokha.smarttale.util.ImageValidator.validateImage;

@Tag(name = "Account", description = "Controller for personal account")
@RestController
@RequestMapping("/v1/account/profile")
public class AccountController {

    private final UserService userService;

    public AccountController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Get own profile", description = "Returns basic information about the user and avatar",
            tags = {"account", "get", "user", "profile"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Profile not found", content = @Content),
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
                                                 @RequestBody @Valid UpdateProfileRequest request) {

        if (!request.isValid()) {
            throw new IllegalArgumentException("Name fields should be either all Latin or all Cyrillic");
        }

        return ResponseEntity.ok(userService.updateProfile(getUserIdFromAuthToken(authentication), request));
    }

    @Operation(
            summary = "Upload avatar", description = "Upload an image using param \"avatar\" to set an avatar",
            tags = {"post", "user", "profile", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "400", description = "Invalid file", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            },
            parameters = @Parameter(name = "avatar", required = true, description = "content type \"image/\"")
    )
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateAvatar(@RequestParam(value = "avatar", required = false) MultipartFile avatar,
                                               Authentication authentication) {

        validateImage(avatar);

        userService.uploadAvatar(avatar, getUserIdFromAuthToken(authentication));

        return ResponseEntity.ok("Avatar updated successfully!");
    }

    @Operation(
            summary = "Subscription", description = "Send subscription request to admin",
            tags = {"post", "user", "profile", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Request success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            }

    )
    @PostMapping("/subscription")
    public ResponseEntity<String> subscribe(Authentication authentication) {
        userService.subscribe(getUserIdFromAuthToken(authentication));

        return ResponseEntity.ok("The subscription is on the way, our administrator will contact you");
    }

    @Operation(
            summary = "Get user's invitations", tags = {"get", "invitation", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping("/invitations")
    public ResponseEntity<CustomPage<Invitation>> getInvitations(Authentication authentication,
                                                                 @RequestParam(required = false, defaultValue = "0") int page,
                                                                 @RequestParam(required = false, defaultValue = "5") int size) {
        return ResponseEntity.ok(userService.getInvitations(getUserIdFromAuthToken(authentication), page, size));
    }

    @Operation(
            summary = "Accept invitation", tags = {"post", "invitation", "account"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Success"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized"),
                    @ApiResponse(responseCode = "403", description = "Still has assigned tasks"),
                    @ApiResponse(responseCode = "404", description = "Invitation not found or expired")
            }
    )
    @PostMapping("/invitations/{invitationId}")
    public ResponseEntity<String> acceptInvitation(Authentication authentication,
                                                   @PathVariable Long invitationId) {
        userService.acceptInvitation(getUserIdFromAuthToken(authentication), invitationId);

        return ResponseEntity.ok("Invitation accepted");
    }

}
