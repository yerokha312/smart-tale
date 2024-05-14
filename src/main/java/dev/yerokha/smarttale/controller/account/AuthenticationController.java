package dev.yerokha.smarttale.controller.account;

import dev.yerokha.smarttale.dto.LoginResponse;
import dev.yerokha.smarttale.dto.RegistrationRequest;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Controller for reg/login/verification etc")
@RestController
@RequestMapping("/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Operation(
            summary = "Registration", description = "Create a new user account",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "201", description = "Registration success"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "409", description = "Email is already taken")
            }
    )
    @PostMapping("/registration")
    public ResponseEntity<String> register(@RequestBody @Valid RegistrationRequest request,
                                           @RequestParam(value = "code", required = false) String code) {

        if (!request.isValid()) {
            throw new IllegalArgumentException("Name fields should be either all Latin or all Cyrillic");
        }

        return new ResponseEntity<>(authenticationService.register(request, code), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Check email availability",
            description = "Endpoint for pre-submit checking of available email. True if available",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns true or false")
            }
    )
    @PostMapping("/email-available")
    public ResponseEntity<Boolean> isEmailAvailable(@RequestBody @Valid @Email String email) {
        boolean emailAvailable = authenticationService.isEmailAvailable(email);
        return ResponseEntity.ok(emailAvailable);
    }

    @Operation(
            summary = "Check phone availability",
            description = "Endpoint for pre-submit checking of available phone. True if available",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns true or false")
            }
    )
    @PostMapping("/phone-available")
    public ResponseEntity<Boolean> isPhoneAvailable(@RequestBody String phoneNumber) {
        boolean phoneAvailable = authenticationService.isPhoneAvailable(phoneNumber);
        return ResponseEntity.ok(phoneAvailable);
    }

    @Operation(
            summary = "Verification", description = "Verify email by entering verification code",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email confirmed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid code", content = @Content),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }

    )
    @PostMapping("/verification")
    public ResponseEntity<LoginResponse> verifyEmail(@RequestBody @Valid VerificationRequest request) {
        return ResponseEntity.ok(authenticationService.verifyEmail(request.email(), request.code()));
    }

    @Operation(
            summary = "Resend mail", description = "Resend mail for user email verification",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email sent"),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE))
    )
    @PostMapping(value = "/resend-verification", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> resend(@RequestBody @Valid @Email String email) {
        authenticationService.sendVerificationEmail(email);
        return ResponseEntity.ok(String.format("Code generated, email sent to %s", email));
    }

    @Operation(
            summary = "Login", description = "EP for sending verification code to email",
            tags = {"post", "authentication"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email sent"),
                    @ApiResponse(responseCode = "400", description = "Bad email"),
                    @ApiResponse(responseCode = "401", description = "User is not enabled (email not verified)"),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            }
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid @Email String email,
                                        @RequestParam(name = "code", required = false) String code) {
        return ResponseEntity.ok(authenticationService.login(email, code));
    }

    @Operation(
            summary = "Refresh", description = "Obtain a new access token using refresh token",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Access token obtained successfully"),
                    @ApiResponse(responseCode = "401", description = "Invalid token exception", content = @Content)
            }
    )
    @PostMapping(value = "/refresh-token", consumes = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody String refreshToken) {
        return ResponseEntity.ok(authenticationService.refreshToken(refreshToken));
    }

    @Operation(
            summary = "Logout", description = "Accepts \"Bearer \" + \"refreshToken\" string in body and " +
            "\"Bearer \" + \"accessToken\" via headers " +
            "for further revocation and logging out",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Revocation and logout success"),
                    @ApiResponse(responseCode = "401", description = "Invalid token")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<String> revoke(@RequestBody String refreshToken, HttpServletRequest request) {
        authenticationService.revoke(refreshToken, request);
        return ResponseEntity.ok("Logout success");
    }

}
