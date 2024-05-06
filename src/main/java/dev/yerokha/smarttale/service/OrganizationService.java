package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.Employee;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.dto.OrderSummary;
import dev.yerokha.smarttale.dto.Organization;
import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.mapper.AdMapper;
import dev.yerokha.smarttale.repository.InvitationRepository;
import dev.yerokha.smarttale.repository.OrderRepository;
import dev.yerokha.smarttale.repository.PositionRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import dev.yerokha.smarttale.util.EncryptionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.lang.Integer.parseInt;

@Service
public class OrganizationService {

    private static final String REG_PAGE = "";
    private static final String LOGIN_PAGE = "";
    private final OrderRepository orderRepository;
    private final MailService mailService;
    private final InvitationRepository invitationRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final PositionRepository positionRepository;

    public OrganizationService(OrderRepository orderRepository, MailService mailService, InvitationRepository invitationRepository, UserDetailsRepository userDetailsRepository, PositionRepository positionRepository) {
        this.orderRepository = orderRepository;
        this.mailService = mailService;
        this.invitationRepository = invitationRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.positionRepository = positionRepository;
    }

    public Page<OrderSummary> getOrders(Long employeeId, Map<String, String> params) {
        Sort sort = getSortProps(params);
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "6")),
                sort.equals(Sort.unsorted()) ?
                        Sort.by(Sort.Direction.DESC, "acceptedAt") : sort);

        Long organizationId = userDetailsRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getOrganization()
                .getOrganizationId();

        boolean isActive = Boolean.parseBoolean(params.getOrDefault("active", "true"));
        return isActive
                ? getActiveOrders(organizationId, params, pageable)
                : getCompletedOrders(organizationId, params, pageable);
    }

    private Page<OrderSummary> getActiveOrders(Long organizationId, Map<String, String> params, Pageable pageable) {
        return getOrdersByDateRange(organizationId, params, pageable, true);
    }

    private Page<OrderSummary> getCompletedOrders(Long organizationId, Map<String, String> params, Pageable pageable) {
        return getOrdersByDateRange(organizationId, params, pageable, false);
    }

    private Page<OrderSummary> getOrdersByDateRange(Long organizationId, Map<String, String> params, Pageable pageable, boolean isActive) {
        String dateType = params.get("dateType");
        if (dateType != null) {
            return switch (dateType) {
                case "accepted" -> getOrdersByAcceptedDate(organizationId, isActive, params, pageable);
                case "deadline" -> getOrdersByDeadlineDate(organizationId, isActive, params, pageable);
                case "completed" -> isActive ? null : getOrdersByCompletedDate(organizationId, params, pageable);
                default -> throw new IllegalArgumentException("Date type is not valid");
            };
        }
        return isActive
                ? orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtIsNull(organizationId, pageable)
                .map(AdMapper::toCurrentOrder)
                : orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtIsNotNull(organizationId, pageable)
                .map(AdMapper::toCurrentOrder);
    }

    private Page<OrderSummary> getOrdersByCompletedDate(Long organizationId,
                                                        Map<String, String> params,
                                                        Pageable pageable) {

        LocalDate exactDate = parseDate(params.get("date"));

        if (exactDate == null) {
            LocalDate startDate = parseDate(params.get("startDate"));
            LocalDate endDate = parseDate(params.get("endDate"));
            return orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtBetween(
                            organizationId,
                            startDate,
                            endDate,
                            pageable)
                    .map(AdMapper::toCurrentOrder);
        }
        return orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAt(
                        organizationId,
                        exactDate,
                        pageable)
                .map(AdMapper::toCurrentOrder);
    }

    private Page<OrderSummary> getOrdersByDeadlineDate(Long organizationId,
                                                       boolean isActive,
                                                       Map<String, String> params,
                                                       Pageable pageable) {

        LocalDate exactDate = parseDate(params.get("date"));

        if (exactDate == null) {
            LocalDate startDate = parseDate(params.get("startDate"));
            LocalDate endDate = parseDate(params.get("endDate"));
            if (isActive) {
                return orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtIsNullAndDeadlineAtBetween(
                                organizationId,
                                startDate,
                                endDate,
                                pageable)
                        .map(AdMapper::toCurrentOrder);
            }
            return orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtIsNotNullAndDeadlineAtBetween(
                    organizationId,
                    startDate,
                    endDate,
                    pageable).map(AdMapper::toCurrentOrder);
        }
        if (isActive) {
            return orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtIsNullAndDeadlineAt(
                    organizationId,
                    exactDate,
                    pageable).map(AdMapper::toCurrentOrder);
        }
        return orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtIsNotNullAndDeadlineAt(
                organizationId,
                exactDate,
                pageable).map(AdMapper::toCurrentOrder);
    }

    private Page<OrderSummary> getOrdersByAcceptedDate(Long organizationId,
                                                       boolean isActive,
                                                       Map<String, String> params,
                                                       Pageable pageable) {

        LocalDate exactDate = parseDate(params.get("date"));

        if (exactDate == null) {
            LocalDate startDate = parseDate(params.get("startDate"));
            LocalDate endDate = parseDate(params.get("endDate"));
            if (isActive) {
                return orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtIsNullAndAcceptedAtBetween(
                        organizationId,
                        startDate,
                        endDate,
                        pageable).map(AdMapper::toCurrentOrder);
            }
            return orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtIsNotNullAndAcceptedAtBetween(
                    organizationId,
                    startDate,
                    endDate,
                    pageable).map(AdMapper::toCurrentOrder);
        }
        if (isActive) {
            return orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtIsNullAndAcceptedAt(
                    organizationId,
                    exactDate,
                    pageable).map(AdMapper::toCurrentOrder);
        }
        return orderRepository.findAllByAcceptedByOrganizationIdAndCompletedAtIsNotNullAndAcceptedAt(
                organizationId,
                exactDate,
                pageable).map(AdMapper::toCurrentOrder);
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr != null) {
            return LocalDate.parse(dateStr);
        }
        return null;
    }


    private Sort getSortProps(Map<String, String> params) {
        List<Sort.Order> orders = new ArrayList<>();
        params.forEach((key, value) -> {
            if (!(key.startsWith("page") || key.startsWith("size") || key.equals("active") || key.toLowerCase().contains("date"))) {
                Sort.Direction direction = value.equalsIgnoreCase("asc") ?
                        Sort.Direction.ASC : Sort.Direction.DESC;
                switch (key) {
                    case "name" -> {
                        orders.add(0, new Sort.Order(direction, "lastName"));
                        orders.add(1, new Sort.Order(direction, "firstName"));
                        orders.add(2, new Sort.Order(direction, "middleName"));
                    }
                    case "position" -> orders.add(0, new Sort.Order(direction, "position"));
                    case "orders" -> orders.add(0, new Sort.Order(direction, "activeOrdersCount"));
                    default -> orders.add(0, new Sort.Order(direction, key));
                }
            }
        });
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    public Page<Employee> getEmployees(Long employeeId, Map<String, String> params) {
        Sort sort = getSortProps(params);
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "6")),
                sort.equals(Sort.unsorted()) ?
                        Sort.by(Sort.Direction.ASC, "lastName", "firstName", "middleName") : sort);

        OrganizationEntity organization = userDetailsRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getOrganization();

        return getEmployees(organization.getOrganizationId(), pageable);

    }

    private Page<Employee> getEmployees(Long organizationId, Pageable pageable) {
        return userDetailsRepository
                .findAllEmployeesAndInvitees(organizationId, pageable)
                .map(user -> {
                    String name = user.getName() == null ? "" : user.getName();
                    List<OrderSummary> orders = getCurrentOrders(user, organizationId);
                    String position = getPosition(user, organizationId);
                    String status = orders == null || name.isEmpty() ? "Invited" : "Authorized";

                    return new Employee(
                            user.getUserId(),
                            name,
                            user.getEmail(),
                            orders,
                            position,
                            status
                    );
                });
    }

    private static String getPosition(UserDetailsEntity employee, Long organizationId) {
        if (employee.getOrganization() == null) {
            return Objects.requireNonNull(employee.getInvitations().stream()
                            .filter(inv -> inv.getOrganization().getOrganizationId().equals(organizationId))
                            .findFirst()
                            .orElse(null))
                    .getPosition()
                    .getTitle();
        }

        if (!employee.getOrganization().getOrganizationId().equals(organizationId)) {
            return employee.getInvitations().stream()
                    .filter(inv -> inv.getOrganization().getOrganizationId().equals(organizationId))
                    .map(invitationEntity -> invitationEntity.getPosition().getTitle())
                    .findFirst()
                    .orElse(null);
        }
        PositionEntity position = employee.getPosition();
        return position.getTitle();
    }

    private static List<OrderSummary> getCurrentOrders(UserDetailsEntity employee,
                                                       Long organizationId) {

        OrganizationEntity organization = employee.getOrganization();
        if ((organization == null) || !organization.getOrganizationId().equals(organizationId)) {
            return null;
        }

        return employee.getAssignedTasks().stream()
                .filter(order -> order.getCompletedAt() == null)
                .map(AdMapper::toCurrentOrder)
                .toList();
    }

    public String inviteEmployee(Long inviterId, InviteRequest request) {
        UserDetailsEntity inviter = userDetailsRepository.findById(inviterId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        OrganizationEntity organization = inviter.getOrganization();
        PositionEntity position = positionRepository.findById(request.positionId())
                .orElseThrow(() -> new NotFoundException("Position not found"));

        UserDetailsEntity invitee = getInvitee(request);
        InvitationEntity invitation = new InvitationEntity(
                LocalDate.now(), inviter, invitee, organization, position);
        invitationRepository.save(invitation);

        String name = invitee.getName();
        String code = EncryptionUtil.encrypt(String.valueOf(invitation.getInvitationId()));
        String link = REG_PAGE + "?code=" + code;
        if (name != null) {
            link = LOGIN_PAGE + "?code=" + code;
        }

        mailService.sendInvitation(request.email(),
                name,
                organization.getName(),
                position.getTitle(),
                link);

        return request.email();

    }

    private UserDetailsEntity getInvitee(InviteRequest request) {
        return userDetailsRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(request.email());
                    newUser.setInvited(true);

                    UserDetailsEntity userDetails = new UserDetailsEntity();
                    userDetails.setUser(newUser);
                    userDetails.setEmail(request.email());

                    String lastName = request.lastName();
                    if (lastName != null) {
                        userDetails.setLastName(lastName);
                    }

                    String firstName = request.firstName();
                    if (firstName != null) {
                        userDetails.setFirstName(firstName);
                    }

                    String middleName = request.middleName();
                    if (middleName != null) {
                        userDetails.setMiddleName(middleName);
                    }

                    return userDetailsRepository.save(userDetails);
                });
    }

    public List<Position> getPositions(Long userId) {
        return userDetailsRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getOrganization()
                .getPositions().stream()
                .sorted(Comparator.comparing(PositionEntity::getTitle))
                .map(pos -> new Position(
                        pos.getPositionId(),
                        pos.getTitle()
                ))
                .toList();
    }

    public Organization getOrganization(Long userId) {
        OrganizationEntity organization = userDetailsRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getOrganization();

        UserDetailsEntity owner = organization.getOwner();
        String avatarUrl = owner.getImage() == null ? null : owner.getImage().getImageUrl();
        String logoUrl = organization.getImage() == null ? null : organization.getImage().getImageUrl();
        return new Organization(
                organization.getOrganizationId(),
                owner.getUserId(),
                owner.getName(),
                avatarUrl,
                organization.getName(),
                organization.getDescription(),
                organization.getRegisteredAt(),
                logoUrl
        );
    }
}