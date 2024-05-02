package dev.yerokha.smarttale.controller.account;

import dev.yerokha.smarttale.dto.Profile;
import dev.yerokha.smarttale.dto.UpdateProfileRequest;
import dev.yerokha.smarttale.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

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
    @PostMapping("/avatar")
    public ResponseEntity<String> updateAvatar(@RequestParam("avatar") MultipartFile avatar,
                                               Authentication authentication) {

        validateImage(avatar);

        userService.uploadAvatar(avatar, getUserIdFromAuthToken(authentication));

        return ResponseEntity.ok("Avatar updated successfully!");
    }

    public static void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is not provided");
        }

        if (!Objects.requireNonNull(file.getContentType()).startsWith("image/")) {
            throw new IllegalArgumentException("Uploaded file is not an image");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file has no name");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png");

        if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Uploaded file is not a supported image (JPG, JPEG, PNG)");
        }
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


}
