package dev.yerokha.smarttale.util;

import dev.yerokha.smarttale.dto.AssignmentRequest;
import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.repository.OrderRepository;
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
import static dev.yerokha.smarttale.util.Authorities.CREATE_POSITION;
import static dev.yerokha.smarttale.util.Authorities.DELETE_EMPLOYEE;
import static dev.yerokha.smarttale.util.Authorities.INVITE_EMPLOYEE;
import static dev.yerokha.smarttale.util.Authorities.UPDATE_POSITION;
import static dev.yerokha.smarttale.util.Authorities.UPDATE_STATUS_FROM_CHECKING;
import static dev.yerokha.smarttale.util.Authorities.UPDATE_STATUS_TO_CHECKING;
import static dev.yerokha.smarttale.util.Authorities.valueOf;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private UserDetailsRepository userDetailsRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        int userHierarchy = getUserHierarchyFromToken(authentication);
        int userAuthorities = getUserAuthoritiesFromToken(authentication);
        if (userAuthorities == 0) {
            return false;
        }
        int requiredPermissionBitmask = valueOf((String) permission).getBitmask();
        if ((requiredPermissionBitmask & userAuthorities) != requiredPermissionBitmask) {
            return false;
        }
        // INVITE_EMPLOYEE duplicated cause this is for positions dropdown, second one is for invite itself
        return switch (permission.toString()) {
            case "INVITE_EMPLOYEE", "CREATE_ORDER", "DELETE_ORDER" -> true;
            case "CREATE_POSITION" -> {
                Position position = (Position) targetDomainObject;
                int positionAuthorities = position.authorities().stream()
                        .map(authString -> valueOf(authString).getBitmask())
                        .reduce(0, (acc, bitmask) -> acc | bitmask);
                yield ((positionAuthorities & userAuthorities) == positionAuthorities)
                        && (userHierarchy < position.hierarchy());
            }
            case "UPDATE_POSITION" -> {
                Position position = (Position) targetDomainObject;
                Long positionId = position.positionId();
                PositionEntity positionEntity = getPosition(positionId);
                if (positionEntity.getHierarchy() <= userHierarchy) {
                    yield false;
                }
                int positionAuthorities = position.authorities().stream()
                        .map(authString -> valueOf(authString).getBitmask())
                        .reduce(0, (acc, bitmask) -> acc | bitmask);
                if ((positionAuthorities & userAuthorities) != positionAuthorities) {
                    yield false;
                }
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
        if (!permissionString.equals("UPDATE_STATUS") && !permissionString.equals("UPDATE_EMPLOYEE")) {
            int requiredPermissionBitmask = valueOf(permissionString).getBitmask();
            if ((requiredPermissionBitmask & userAuthorities) != requiredPermissionBitmask) {
                return false;
            }
        }
        // INVITE_EMPLOYEE duplicated cause this is for invite itself, first one is for positions dropdown
        return switch (permissionString) {
            case "INVITE_EMPLOYEE", "DELETE_POSITION" -> {
                Long positionId = (Long) targetId;
                PositionEntity position = getPosition(positionId);
                yield hasPermission(userHierarchy, userAuthorities, position);
            }
            case "UPDATE_STATUS" -> {
                boolean userCanMoveToChecking = (userAuthorities & UPDATE_STATUS_TO_CHECKING.getBitmask()) > 0;
                boolean userCanMoveFromChecking = (userAuthorities & UPDATE_STATUS_FROM_CHECKING.getBitmask()) > 0;
                // if user has not any of these permissions returns false (403) immediately
                if (!userCanMoveToChecking && !userCanMoveFromChecking) {
                    yield false;
                }
                Long orderId = (Long) targetId;
                OrderEntity order = orderRepository.findById(orderId)
                        .orElseThrow(() -> new NotFoundException("Order not found"));

                yield hasPermission(order, userCanMoveFromChecking);
            }
            case "DELETE_EMPLOYEE" -> {
                Long employeeId = (Long) targetId;
                UserDetailsEntity employee = getEmployee(employeeId);

                PositionEntity employeePosition = employee.getPosition();

                yield hasPermission(employeePosition, userHierarchy, userAuthorities);
            }

            case "UPDATE_EMPLOYEE" -> {
                int createPositionBitmask = CREATE_POSITION.getBitmask();
                int updatePositionBitmask = UPDATE_POSITION.getBitmask();
                int inviteEmployeeBitmask = INVITE_EMPLOYEE.getBitmask();
                int deleteEmployeeBitmask = DELETE_EMPLOYEE.getBitmask();
                int requiredPermissionsBitmask = createPositionBitmask | updatePositionBitmask | inviteEmployeeBitmask | deleteEmployeeBitmask;

                boolean hasRequiredPermissions = (userAuthorities & requiredPermissionsBitmask) != 0;

                if (!hasRequiredPermissions) {
                    yield false;
                }
                Long employeeId = (Long) targetId;
                Long positionId = Long.valueOf(targetType);
                UserDetailsEntity employee = getEmployee(employeeId);
                if (userHierarchy >= employee.getPosition().getHierarchy()) {
                    yield false;
                }
                PositionEntity position = getPosition(positionId);
                if (userHierarchy >= position.getHierarchy()) {
                    yield false;
                }
                yield ((userAuthorities & position.getAuthorities()) == position.getAuthorities());
            }
            default -> throw new IllegalStateException("Unexpected value: " + permissionString);
        };
    }

    private PositionEntity getPosition(Long positionId) {
        return positionRepository.findById(positionId)
                .orElseThrow(() -> new NotFoundException("Position nof found"));
    }

    private UserDetailsEntity getEmployee(Long employeeId) {
        return userDetailsRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    private static boolean hasPermission(PositionEntity employeePosition, int userHierarchy, int userAuthorities) {
        int employeeHierarchy = employeePosition.getHierarchy();
        int employeeAuthorities = employeePosition.getAuthorities();
        return userHierarchy < employeeHierarchy
                && ((userAuthorities & employeeAuthorities) == employeeAuthorities);
    }

    private static boolean hasPermission(OrderEntity order, boolean userCanMoveFromChecking) {
        OrderStatus orderStatus = order.getStatus();

        boolean orderStatusIsRestricted = orderStatus == OrderStatus.PENDING ||
                orderStatus == OrderStatus.COMPLETED ||
                orderStatus == OrderStatus.CANCELED;

        if (orderStatusIsRestricted) {
            return false;
        }

        boolean orderStatusIsCheckingOrHigher = orderStatus.compareTo(OrderStatus.CHECKING) >= 0;
        return userCanMoveFromChecking || !orderStatusIsCheckingOrHigher;
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
