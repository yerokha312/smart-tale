package dev.yerokha.smarttale.util;

import dev.yerokha.smarttale.dto.AssignmentRequest;
import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.repository.PositionRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

import static dev.yerokha.smarttale.service.TokenService.getUserAuthoritiesFromToken;
import static dev.yerokha.smarttale.service.TokenService.getUserHierarchyFromToken;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private UserDetailsRepository userDetailsRepository;

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
            case "INVITE_EMPLOYEE", "CREATE_ORDER" -> true;
            case "CREATE_POSITION", "UPDATE_POSITION" -> {
                Position position = (Position) targetDomainObject;
                int positionAuthorities = position.authorities().stream()
                        .map(authString -> Authorities.valueOf(authString).getBitmask())
                        .reduce(0, (acc, bitmask) -> acc | bitmask);
                yield ((positionAuthorities & userAuthorities) == positionAuthorities)
                        && (userHierarchy < position.hierarchy());
            }
            case "ASSIGN_EMPLOYEES" -> {
                AssignmentRequest request = (AssignmentRequest) targetDomainObject;
                List<UserDetailsEntity> contractors = userDetailsRepository.findAllById(request.employeeIds());
                yield contractors.stream()
                        .map(UserDetailsEntity::getPosition)
                        .allMatch(contractorPosition -> contractorPosition.getHierarchy() > userHierarchy);
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
        String permissionString = (String) permission;
        int requiredPermissionBitmask = Authorities.valueOf(permissionString).getBitmask();
        if ((requiredPermissionBitmask & userAuthorities) != requiredPermissionBitmask) {
            return false;
        }
        return switch (permissionString) {
            case "INVITE_EMPLOYEE" -> {
                Long positionId = (Long) targetId;
                PositionEntity position = positionRepository.findById(positionId).orElse(null);
                yield hasPermission(userHierarchy, userAuthorities, position);
            }
            default -> throw new IllegalStateException("Unexpected value: " + permissionString);
        };
    }

    private boolean hasPermission(int userHierarchy, int userAuthorities, PositionEntity position) {
        if (position == null) {
            return false;
        }
        int positionAuthorities = position.getAuthorities();
        return userHierarchy < position.getHierarchy()
                && ((positionAuthorities & userAuthorities) == positionAuthorities);
    }
}
