package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.LoginResponse;
import dev.yerokha.smarttale.entity.RefreshToken;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.enums.TokenType;
import dev.yerokha.smarttale.exception.InvalidTokenException;
import dev.yerokha.smarttale.repository.TokenRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.yerokha.smarttale.util.EncryptionUtil.decrypt;
import static dev.yerokha.smarttale.util.EncryptionUtil.encrypt;
import static dev.yerokha.smarttale.util.RedisUtil.containsKey;
import static dev.yerokha.smarttale.util.RedisUtil.deleteKey;
import static dev.yerokha.smarttale.util.RedisUtil.setValue;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenRepository tokenRepository;

    private static final int ACCESS_TOKEN_EXPIRATION = 15;
    private static final int REFRESH_TOKEN_EXPIRATION = ACCESS_TOKEN_EXPIRATION * 4 * 24 * 7;

    public TokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, TokenRepository tokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.tokenRepository = tokenRepository;
    }

    public String generateAccessToken(UserEntity entity) {
        String accessToken = generateToken(entity, ACCESS_TOKEN_EXPIRATION, TokenType.ACCESS);
        setValue("access_token:" + entity.getUsername(),
                encrypt(accessToken),
                ACCESS_TOKEN_EXPIRATION,
                TimeUnit.MINUTES);
        return accessToken;
    }

    public String generateRefreshToken(UserEntity entity) {
        String refreshToken = generateToken(entity, REFRESH_TOKEN_EXPIRATION, TokenType.REFRESH);
        String encryptedToken = encrypt("Bearer " + refreshToken);
        RefreshToken refreshTokenEntity = new RefreshToken(
                encryptedToken,
                entity,
                Instant.now(),
                Instant.now().plus(REFRESH_TOKEN_EXPIRATION, ChronoUnit.MINUTES)
        );
        tokenRepository.save(refreshTokenEntity);
        return refreshToken;
    }

    private String generateToken(UserEntity entity, int expirationTime, TokenType tokenType) {
        Instant now = Instant.now();
        String scopes = getScopes(entity);

        JwtClaimsSet claims = getClaims(now,
                expirationTime,
                entity.getUsername(),
                entity.getUserId(),
                scopes,
                tokenType);
        return encodeToken(claims);
    }

    private String getScopes(UserEntity entity) {
        return entity.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }

    private JwtClaimsSet getClaims(Instant now, long expirationTime, String subject, Long userId, String scopes, TokenType tokenType) {
        return JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expirationTime, ChronoUnit.MINUTES))
                .subject(subject)
                .claim("scopes", scopes)
                .claim("tokenType", tokenType)
                .claim("userId", userId)
                .build();
    }

    private String encodeToken(JwtClaimsSet claims) {
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String getEmailFromToken(String token) {
        return decodeToken(token).getSubject();
    }

    private Jwt decodeToken(String token) {
        if (!token.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid token format");
        }

        String strippedToken = token.substring(7);

        try {
            return jwtDecoder.decode(strippedToken);
        } catch (InvalidTokenException e) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    public static Long getUserIdFromAuthToken(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("userId");
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        Jwt decodedToken = decodeToken(refreshToken);
        String email = decodedToken.getSubject();
        if (!decodedToken.getClaim("tokenType").equals(TokenType.REFRESH.name())) {
            throw new InvalidTokenException("Invalid token type");
        }

        if (isExpired(decodedToken)) {
            throw new InvalidTokenException("Refresh token expired");
        }

        if (isRevoked(refreshToken, email)) {
            throw new InvalidTokenException("Token is revoked");
        }

        Instant now = Instant.now();
        String subject = decodedToken.getSubject();
        Long userId = decodedToken.getClaim("userId");
        String scopes = decodedToken.getClaim("scopes");
        JwtClaimsSet claims = getClaims(now,
                ACCESS_TOKEN_EXPIRATION,
                subject,
                userId,
                scopes,
                TokenType.ACCESS);
        String token = encodeToken(claims);
        String key = "access_token:" + email;
        setValue(key, encrypt(token), ACCESS_TOKEN_EXPIRATION, TimeUnit.MINUTES);
        return new LoginResponse(
                token,
                refreshToken.substring(7),
                userId

        );

    }

    private boolean isRevoked(String refreshToken, String email) {
        List<RefreshToken> tokenList = tokenRepository.findNotRevokedByEmail(email);
        if (tokenList.isEmpty()) {
            return true;
        }

        for (RefreshToken token : tokenList) {
            if (refreshToken.equals(decrypt(token.getToken()))) {
                return false;
            }
        }

        return true;
    }

    private boolean isExpired(Jwt decodedToken) {
        return Objects.requireNonNull(decodedToken.getExpiresAt()).isBefore(Instant.now());
    }

    public void revokeToken(String token) {
        String email = decodeToken(token).getSubject();
        String key = "access_token:" + email;
        if (containsKey(key)) {
            deleteKey(key);
            return;
        }
        List<RefreshToken> notRevokedByUsername = tokenRepository.findNotRevokedByEmail(email);
        for (RefreshToken refreshToken : notRevokedByUsername) {
            if (token.equals(decrypt(refreshToken.getToken()))) {
                refreshToken.setRevoked(true);
                tokenRepository.save(refreshToken);
                return;
            }
        }
    }

    public void revokeAllTokens(String email) {
        deleteKey("access_token:" + email);
        List<RefreshToken> notRevokedByUsername = tokenRepository.findNotRevokedByEmail(email);
        notRevokedByUsername.forEach(token -> token.setRevoked(true));
        tokenRepository.saveAll(notRevokedByUsername);
    }
}

