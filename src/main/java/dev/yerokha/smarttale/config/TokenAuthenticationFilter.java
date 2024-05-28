package dev.yerokha.smarttale.config;

import dev.yerokha.smarttale.service.TokenService;
import dev.yerokha.smarttale.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static dev.yerokha.smarttale.util.EncryptionUtil.decrypt;
import static dev.yerokha.smarttale.util.RedisUtil.*;

@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;

    public TokenAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String accessToken = request.getHeader("Authorization");
        if (accessToken == null || !accessToken.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String email;

        try {
            email = tokenService.getEmailFromToken(accessToken);
            String key = "access_token:" + email;
            String cachedToken = decrypt(getValue(key));
            String accessTokenValue = accessToken.substring(7);
            if (!accessTokenValue.equals(cachedToken)) {
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token " + e.getMessage());
            return;
        }
//
//        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//            UserDetails userDetails = userService.loadUserByUsername(email);
//
//            if (userDetails != null) {
//                String key = "access_token:" + email;
//
//                if (containsKey(key)) {
//                    String cachedToken = decrypt(getValue(key));
//                    final String tokenValue = accessToken.substring(7);
//
//                    if (tokenValue.equals(cachedToken)) {
//                        UsernamePasswordAuthenticationToken authenticationToken =
//                                new UsernamePasswordAuthenticationToken(userDetails, null);
//                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//                    } else {
//                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                        return;
//                    }
//                } else {
//                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//                    return;
//                }
//            }
//        }

        filterChain.doFilter(request, response);
    }
}
