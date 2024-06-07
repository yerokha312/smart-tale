package dev.yerokha.smarttale.util;

import dev.yerokha.smarttale.service.TokenService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class TokenRevocationAspect {

    private final TokenService tokenService;

    public TokenRevocationAspect(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @AfterReturning(pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.createOrganization(..))",
            returning = "email")
    public void afterCreateOrganization(String email) {
        revokeTokens(email);
    }

    @AfterReturning(pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.updateEmployee(..))",
            returning = "email")
    public void afterUpdateEmployee(String email) {
        revokeTokens(email);
    }

    @AfterReturning(pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.deleteEmployee(..))",
            returning = "email")
    public void afterDeleteEmployee(String email) {
        revokeTokens(email);
    }

    @AfterReturning(pointcut = "execution(* dev.yerokha.smarttale.service.OrganizationService.updatePosition(..))",
            returning = "emailListForRevocation")
    public void afterUpdatePosition(List<String> emailListForRevocation) {
        emailListForRevocation.forEach(this::revokeTokens);
    }

    @AfterReturning(pointcut = "execution(* dev.yerokha.smarttale.service.UserService.acceptInvitation(..))",
            returning = "email")
    public void afterAcceptInvitation(String email) {
        revokeTokens(email);
    }

    public void revokeTokens(String email) {
        tokenService.revokeAllTokens(email);

    }
}