package dev.yerokha.smarttale.util;

import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.dto.UpdateTaskRequest;
import dev.yerokha.smarttale.entity.advertisement.JobEntity;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.repository.InvitationRepository;
import dev.yerokha.smarttale.repository.JobRepository;
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
    @Autowired
    private InvitationRepository invitationRepository;
    @Autowired
    private JobRepository jobRepository;

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
                if (targetDomainObject.equals("getEmployeesBeforeAssign")) {
                    yield (userAuthorities & requiredPermissionBitmask) > 0;
                }
                UpdateTaskRequest request = (UpdateTaskRequest) targetDomainObject;
                List<UserDetailsEntity> contractors = userDetailsRepository.findAllById(request.addedEmployees());
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
                if (targetType.equals("JobEntity")) {
                    Long jobId = (Long) targetId;
                    JobEntity job = jobRepository.findById(jobId)
                            .orElseThrow(() -> new NotFoundException("Job not found"));
                    PositionEntity position = job.getPosition();
                    yield hasPermission(position, userHierarchy, userAuthorities);
                }
                Long positionId = (Long) targetId;
                PositionEntity position = getPosition(positionId);
                yield hasPermission(position, userHierarchy, userAuthorities);
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
                if (targetType.equals("Invitation")) {
                    Long invId = (Long) targetId;
                    InvitationEntity invitation = getInvitation(invId);
                    PositionEntity position = invitation.getPosition();
                    yield hasPermission(position, userHierarchy, userAuthorities);
                }
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

    // evaluates permission for deleting employees, deleting invitations, updating job ads
    private boolean hasPermission(PositionEntity position, int userHierarchy, int userAuthorities) {
        if (position == null) {
            return false;
        }
        int employeeHierarchy = position.getHierarchy();
        int employeeAuthorities = position.getAuthorities();
        return userHierarchy < employeeHierarchy
               && ((userAuthorities & employeeAuthorities) == employeeAuthorities);
    }

    private boolean hasPermission(OrderEntity order, boolean userCanMoveFromChecking) {
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

    private InvitationEntity getInvitation(Long invId) {
        return invitationRepository.findById(invId)
                .orElseThrow(() -> new NotFoundException("Invitation not found"));
    }

    private PositionEntity getPosition(Long positionId) {
        return positionRepository.findById(positionId)
                .orElseThrow(() -> new NotFoundException("Position nof found"));
    }

    private UserDetailsEntity getEmployee(Long employeeId) {
        return userDetailsRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }
}
