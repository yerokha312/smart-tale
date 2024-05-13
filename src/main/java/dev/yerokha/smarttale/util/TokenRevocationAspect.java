package dev.yerokha.smarttale.util;

import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.service.TokenService;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

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
            returning = "position")
    public void afterUpdatePosition(PositionEntity position) {
        Set<String> employeeEmails = position.getEmployees().stream()
                .map(UserDetailsEntity::getEmail)
                .collect(Collectors.toSet());
        employeeEmails.forEach(this::revokeTokens);
    }

    public void revokeTokens(String email) {
        tokenService.revokeAllTokens(email);

    }
}