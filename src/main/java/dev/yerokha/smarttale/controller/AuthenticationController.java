package dev.yerokha.smarttale.controller;

import dev.yerokha.smarttale.dto.LoginResponse;
import dev.yerokha.smarttale.dto.RegistrationRequest;
import dev.yerokha.smarttale.dto.VerificationRequest;
import dev.yerokha.smarttale.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public ResponseEntity<String> register(@RequestBody @Valid RegistrationRequest request) {
        String email = authenticationService.register(request);
        return new ResponseEntity<>(String.format(
                "Confirmation link generated, email sent to %s", email), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Check availability",
            description = "Endpoint for pre-submit checking of available email. True if available",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns true or false")
            }
    )
    @PostMapping("/email-available")
    public ResponseEntity<Boolean> checkAvailable(@RequestBody @Valid @Email String email) {
        boolean emailAvailable = authenticationService.isEmailAvailable(email);
        return ResponseEntity.ok(emailAvailable);
    }

    @Operation(
            summary = "Verification", description = "Verify email by entering verification code",
            tags = {"authentication", "post"},
            responses = {
                    @ApiResponse(responseCode = "200", description = "Email confirmed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid code"),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
            },
            parameters = {
                    @Parameter(name = "ct", description = "Encrypted token value", required = true)
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
                    @ApiResponse(responseCode = "404", description = "User not found")
            }
    )
    @PostMapping("/resend-verification")
    public ResponseEntity<String> resend(@RequestBody @Valid @Email String email) {
        authenticationService.sendVerificationEmail(email);
        return ResponseEntity.ok(String.format("Confirmation link generated, email sent to %s", email));
    }

//    @Operation(
//            summary = "Login", description = "Authenticate user and get access & refresh tokens",
//            tags = {"authentication", "post"},
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "User authenticated successfully"),
//                    @ApiResponse(responseCode = "401", description = "Invalid username or password", content = @Content),
//                    @ApiResponse(responseCode = "401", description = "Not enabled", content = @Content),
//            }
//    )
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
//        return ResponseEntity.ok(authenticationService.login(request));
//    }
//
//    @Operation(
//            summary = "Refresh", description = "Obtain a new access token using refresh token",
//            tags = {"authentication", "post"},
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Access token obtained successfully"),
//                    @ApiResponse(responseCode = "401", description = "Invalid token exception", content = @Content)
//            }
//    )
//    @PostMapping("/refresh-token")
//    public ResponseEntity<LoginResponse> refreshToken(@RequestBody String refreshToken) {
//        return ResponseEntity.ok(authenticationService.refreshToken(refreshToken));
//    }


//    @Operation(
//            summary = "Logout", description = "Accepts plain refresh token string in body and Access token via headers " +
//            "for further revocation and logging out",
//            tags = {"authentication", "post"},
//            responses = {
//                    @ApiResponse(responseCode = "200", description = "Revocation and logout success"),
//                    @ApiResponse(responseCode = "401", description = "Invalid token")
//            }
//    )
//    @PostMapping("/logout")
//    public ResponseEntity<String> revoke(@RequestBody String refreshToken, HttpServletRequest request) {
//        authenticationService.revoke(refreshToken, request);
//        return ResponseEntity.ok("Logout success");
//    }

//
//    @PostMapping("/recovery")
//    public ResponseEntity<String> recover(@RequestBody AccountRecoveryRequest request) {
//        authenticationService.recover(request);
//        return ResponseEntity.ok(String.format("Recovery email sent to %s", request.email()));
//    }
//
//    @PutMapping("/recovery")
//    public ResponseEntity<String> recover(@RequestParam("are") String encryptedToken) {
//        authenticationService.recover(encryptedToken);
//        return ResponseEntity.ok("Account recovery success");
//    }
}
