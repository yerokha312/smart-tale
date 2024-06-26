package dev.yerokha.smarttale.service;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import dev.yerokha.smarttale.dto.LoginResponse;
import dev.yerokha.smarttale.entity.RefreshToken;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.enums.TokenType;
import dev.yerokha.smarttale.exception.InvalidTokenException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.repository.TokenRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import dev.yerokha.smarttale.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static dev.yerokha.smarttale.util.EncryptionUtil.decrypt;
import static dev.yerokha.smarttale.util.EncryptionUtil.encrypt;
import static dev.yerokha.smarttale.util.RedisUtil.containsKey;
import static dev.yerokha.smarttale.util.RedisUtil.deleteKey;
import static dev.yerokha.smarttale.util.RedisUtil.setValue;
import static org.aspectj.runtime.internal.Conversions.intValue;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;

    private static final int ACCESS_TOKEN_EXPIRATION = 60;
    private static final int REFRESH_TOKEN_EXPIRATION = ACCESS_TOKEN_EXPIRATION * 24 * 7;

    public TokenService(JwtEncoder jwtEncoder, JwtDecoder jwtDecoder, TokenRepository tokenRepository, UserRepository userRepository, UserDetailsRepository userDetailsRepository) {
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
    }

    static String getEmailFromAuthToken(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject();
    }

    public String generateAccessToken(UserEntity entity, PositionEntity position) {
        String accessToken = generateToken(entity, ACCESS_TOKEN_EXPIRATION, TokenType.ACCESS, position);
        setValue("access_token:" + entity.getEmail(),
                encrypt(accessToken),
                ACCESS_TOKEN_EXPIRATION,
                TimeUnit.MINUTES);
        return accessToken;
    }

    public String generateRefreshToken(UserEntity entity, PositionEntity position) {
        String refreshToken = generateToken(entity, REFRESH_TOKEN_EXPIRATION, TokenType.REFRESH, position);
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

    private String generateToken(UserEntity entity, int expirationTime, TokenType tokenType, PositionEntity position) {
        Instant now = Instant.now();
        String roles = getRolesString(entity);

        long organizationId = 0;
        int hierarchy = 0;
        int authorities = 0;
        if (position != null) {
            OrganizationEntity organization = position.getOrganization();
            organizationId = organization == null ? 0 : organization.getOrganizationId();
            hierarchy = position.getHierarchy();
            authorities = position.getAuthorities();
        }
        JwtClaimsSet claims = getClaims(
                now,
                expirationTime,
                entity.getEmail(),
                entity.getUserId(),
                roles,
                organizationId,
                hierarchy,
                authorities,
                tokenType);
        return encodeToken(claims);
    }

    private String getRolesString(UserEntity entity) {
        return entity.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
    }

    private JwtClaimsSet getClaims(Instant now,
                                   int expirationTime,
                                   String email,
                                   Long userId,
                                   String roles,
                                   long organizationId,
                                   int hierarchy,
                                   int authorities,
                                   TokenType tokenType) {
        return JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(expirationTime, ChronoUnit.MINUTES))
                .subject(email)
                .claim("roles", roles)
                .claim("orgId", organizationId)
                .claim("hierarchy", hierarchy)
                .claim("authorities", authorities)
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

    public Long getUserIdFromToken(String token) {
        return decodeToken(token).getClaim("userId");
    }

    public Long getUserIdFromTokenIgnoringExpiration(String token) {
        if (!token.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid token format");
        }

        String strippedToken = token.substring(7);

        try {
            JWTClaimsSet claimsSet = JWTParser.parse(strippedToken).getJWTClaimsSet();
            return claimsSet.getLongClaim("userId");
        } catch (ParseException e) {
            throw new InvalidTokenException("Invalid token");
        }
    }

    private Jwt decodeToken(String token) {
        if (!token.startsWith("Bearer ")) {
            throw new InvalidTokenException("Invalid token format");
        }

        String strippedToken = token.substring(7);

        try {
            return jwtDecoder.decode(strippedToken);
        } catch (InvalidTokenException | JwtException e) {
            throw new InvalidTokenException(e.getMessage());
        }
    }

    public static Long getUserIdFromAuthToken(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("userId");
    }

    public static Long getOrgIdFromAuthToken(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("orgId");
    }

    public static Integer getUserHierarchyFromToken(Authentication authentication) {
        if (authentication == null) {
            return Integer.MAX_VALUE;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return intValue(jwt.getClaim("hierarchy"));
    }

    public static Integer getUserAuthoritiesFromToken(Authentication authentication) {
        if (authentication == null) {
            return 0;
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return intValue(jwt.getClaim("authorities"));
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        Jwt decodedToken = decodeToken(refreshToken);
        String email = decodedToken.getSubject();
        if (!decodedToken.getClaim("tokenType").equals(TokenType.REFRESH.name())) {
            throw new InvalidTokenException("Invalid token type");
        }

        if (isRevoked(refreshToken, email)) {
            return getNewTokenPair(decodedToken, email);
        }

        return getNewAccessToken(refreshToken, decodedToken, email);

    }

    private LoginResponse getNewTokenPair(Jwt decodedToken, String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.isDeleted()) {
            throw new InvalidTokenException("User is deleted");
        }

        int tokenAuthorities = intValue(decodedToken.getClaim("authorities"));
        int tokenHierarchy = intValue(decodedToken.getClaim("hierarchy"));

        UserDetailsEntity userDetails = userDetailsRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        PositionEntity position = userDetails.getPosition();
        if (position == null) { // case employee was deleted from organization
            if (tokenAuthorities > 0) {
                PositionEntity emptyPosition = new PositionEntity(null, 0, 0, null);
                return new LoginResponse(
                        generateAccessToken(user, emptyPosition),
                        generateRefreshToken(user, emptyPosition)
                );
            } else { // case user (not employee) logged out and using old refresh
                throw new InvalidTokenException("Token is revoked");
            }
        }
        int hierarchy = position.getHierarchy();
        int authorities = position.getAuthorities();

        if (!decodedToken.getClaim("roles").equals(getRolesString(user))
            || tokenHierarchy != hierarchy
            || tokenAuthorities != authorities) {

            return generateNewTokenPair(user, position);
        }

        throw new InvalidTokenException("Token is revoked");
    }

    private LoginResponse generateNewTokenPair(UserEntity user, PositionEntity position) {
        String accessToken = generateAccessToken(user, position);
        String refreshToken = generateRefreshToken(user, position);

        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }

    private LoginResponse getNewAccessToken(String refreshToken, Jwt decodedToken, String email) {
        Instant now = Instant.now();
        String subject = decodedToken.getSubject();
        Long userId = decodedToken.getClaim("userId");
        String roles = decodedToken.getClaim("roles");
        long organizationId = decodedToken.getClaim("orgId");
        int hierarchy = intValue(decodedToken.getClaim("hierarchy"));
        int authorities = intValue(decodedToken.getClaim("authorities"));
        JwtClaimsSet claims = getClaims(now,
                ACCESS_TOKEN_EXPIRATION,
                subject,
                userId,
                roles,
                organizationId,
                hierarchy,
                authorities,
                TokenType.ACCESS);
        String token = encodeToken(claims);
        String key = "access_token:" + email;
        setValue(key, encrypt(token), ACCESS_TOKEN_EXPIRATION, TimeUnit.MINUTES);
        return new LoginResponse(
                token,
                refreshToken.substring(7)
        );
    }

    private boolean isRevoked(String refreshToken, String email) {
        if (isTokenRevoked(refreshToken)) {
            return true;
        }

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

    private boolean isTokenRevoked(String refreshToken) {
        return containsKey("revoked_token:" + refreshToken.substring(7));
    }

    public void revokeToken_Logout(String token) {
        String email = decodeToken(token).getSubject();
        String key = "access_token:" + email;
        if (containsKey(key)) {
            deleteKey(key);
        }
        List<RefreshToken> notRevokedByUsername = tokenRepository.findNotRevokedByEmail(email);
        for (RefreshToken refreshToken : notRevokedByUsername) {
            String decryptedTokenValue = decrypt(refreshToken.getToken());
            if (token.equals(decryptedTokenValue)) {
                refreshToken.setRevoked(true);
                setValue("revoked_token:" + decryptedTokenValue.substring(7), "true", 7, TimeUnit.DAYS);
                tokenRepository.save(refreshToken);
            }
        }
    }

    public void revokeAllTokens(String email) {
        deleteKey("access_token:" + email);
        List<RefreshToken> notRevokedByUsername = tokenRepository.findNotRevokedByEmail(email);
        notRevokedByUsername.forEach(token -> {
            token.setRevoked(true);
            tokenRepository.save(token);
            setValue("revoked_token:" + decrypt(token.getToken()).substring(7), "true", 7, TimeUnit.DAYS);
        });
    }

    public Authentication getAuthentication(String token) {
        UserDetails userDetails = getUserDetails(token);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private UserDetails getUserDetails(String token) {
        Jwt jwt = decodeToken(token);
        String username = jwt.getSubject();
        var authorities = getGrantedAuthoritiesFromString(jwt.getClaim("roles"));

        return new User(username, "", authorities);
    }

    private Collection<GrantedAuthority> getGrantedAuthoritiesFromString(String rolesString) {
        return Arrays.stream(rolesString.split(" "))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}

