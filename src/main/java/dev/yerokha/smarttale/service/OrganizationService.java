package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.CurrentOrder;
import dev.yerokha.smarttale.dto.Employee;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.yerokha.smarttale.enums.OrderStatus.ARRIVED;
import static dev.yerokha.smarttale.enums.OrderStatus.CANCELED;
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

    public Page<CurrentOrder> getOrders(Long employeeId, Map<String, String> params) {
        Sort sort = getSort(params);
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "6")),
                sort.equals(Sort.unsorted()) ?
                        Sort.by(Sort.Direction.DESC, "acceptedAt") : sort);

        List<OrderStatus> inactiveStatuses = Arrays.asList(ARRIVED, CANCELED);

        OrganizationEntity organization = userDetailsRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getOrganization();

        Set<Long> employeeIds = organization.getEmployees().stream()
                .map(UserDetailsEntity::getUserId)
                .collect(Collectors.toSet());

        return orderRepository
                .findAllByAcceptedByUserIdInAndStatusNotIn(
                        employeeIds,
                        inactiveStatuses,
                        pageable)
                .map(AdMapper::toCurrentOrder);
    }

    private Sort getSort(Map<String, String> params) {
        List<Sort.Order> orders = new ArrayList<>();
        params.forEach((key, value) -> {
            if (!(key.startsWith("page") || key.startsWith("size"))) {
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
        Sort sort = getSort(params);
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "6")),
                sort.equals(Sort.unsorted()) ?
                        Sort.by(Sort.Direction.ASC, "lastName", "firstName", "middleName") : sort);

        OrganizationEntity organization = userDetailsRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getOrganization();

        List<OrderStatus> inactiveStatuses = Arrays.asList(ARRIVED, CANCELED);

        return getEmployees(organization.getOrganizationId(), inactiveStatuses, pageable);

    }

    private Page<Employee> getEmployees(Long organizationId, List<OrderStatus> inactiveStatuses, Pageable pageable) {
        return userDetailsRepository
                .findAllEmployeesAndInvitees(organizationId, pageable)
                .map(user -> {
                    String name = user.getName() == null ? "" : user.getName();
                    List<CurrentOrder> orders = getCurrentOrders(user, inactiveStatuses, organizationId);
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

    private static List<CurrentOrder> getCurrentOrders(UserDetailsEntity employee,
                                                       List<OrderStatus> inactiveStatuses,
                                                       Long organizationId) {

        OrganizationEntity organization = employee.getOrganization();
        if ((organization == null) || !organization.getOrganizationId().equals(organizationId)) {
            return null;
        }

        return employee.getAcceptedOrders().stream()
                .filter(order -> !inactiveStatuses.contains(order.getStatus()))
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
        InvitationEntity invitation = new InvitationEntity(LocalDate.now(), inviter, invitee, organization, position);
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
}