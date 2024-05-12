package dev.yerokha.smarttale.util;

import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.repository.PositionRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

import static dev.yerokha.smarttale.service.TokenService.getUserAuthoritiesFromToken;
import static dev.yerokha.smarttale.service.TokenService.getUserHierarchyFromToken;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final PositionRepository positionRepository;

    public CustomPermissionEvaluator(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }


    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        int userHierarchy = getUserHierarchyFromToken(authentication);
        int userAuthorities = getUserAuthoritiesFromToken(authentication);
        if (userAuthorities == 0) {
            return false;
        }
        int requiredPermissionBitmask = Authorities.valueOf((String) permission).getBitmask();
        if ((requiredPermissionBitmask & userAuthorities) != requiredPermissionBitmask) {
            return false;
        }

        return switch (permission.toString()) {
            case "CREATE_ORDER" -> true;
            case "CREATE_POSITION", "UPDATE_POSITION" -> {
                Position position = (Position) targetDomainObject;
                int positionAuthorities = position.authorities().stream()
                        .map(authString -> Authorities.valueOf(authString).getBitmask())
                        .reduce(0, (acc, bitmask) -> acc | bitmask);
                yield ((positionAuthorities & userAuthorities) == positionAuthorities)
                        && (userHierarchy < position.hierarchy());
            }
            default -> throw new IllegalStateException("Unexpected value: " + permission);
        };
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        int userHierarchy = getUserHierarchyFromToken(authentication);
        int userAuthorities = getUserAuthoritiesFromToken(authentication);
        if (userAuthorities == 0) {
            return false;
        }
        int requiredPermissionBitmask = Authorities.valueOf((String) permission).getBitmask();
        if ((requiredPermissionBitmask & userAuthorities) != requiredPermissionBitmask) {
            return false;
        }

        if (permission.equals("INVITE_EMPLOYEE")) {
            Long positionId = (Long) targetId;
            PositionEntity position = positionRepository.findById(positionId).orElse(null);
            return hasPermission(userHierarchy, position);
        }


        return false;
    }

    private boolean hasPermission(int userHierarchy, PositionEntity position) {
        if (position == null) {
            return false;
        }

        return userHierarchy < position.getHierarchy();
    }
}
