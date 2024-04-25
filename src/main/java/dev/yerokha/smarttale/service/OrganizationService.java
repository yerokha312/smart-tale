package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.CurrentOrder;
import dev.yerokha.smarttale.dto.Employee;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.Position;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.mapper.AdMapper;
import dev.yerokha.smarttale.repository.InvitationRepository;
import dev.yerokha.smarttale.repository.OrderRepository;
import dev.yerokha.smarttale.repository.OrganizationRepository;
import dev.yerokha.smarttale.repository.PositionRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.yerokha.smarttale.enums.OrderStatus.ARRIVED;
import static dev.yerokha.smarttale.enums.OrderStatus.CANCELED;
import static java.lang.Integer.parseInt;

@Service
public class OrganizationService {

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
                orders.add(0, new Sort.Order(direction, key));
            }
        });
        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }

    public Page<Employee> getEmployees(Long employeeId, Map<String, String> params) {
        Sort sort = getSort(params);
        Pageable pageable;
        if (sort.equals(Sort.unsorted()) ||
                (sort.isSorted() && sort.stream().allMatch(order -> order.getProperty().equals("name")))) {
            Sort.Direction direction = Sort.Direction.ASC;
            if (sort.isSorted() && sort.stream().anyMatch(order -> order.getDirection() == Sort.Direction.DESC)) {
                direction = Sort.Direction.DESC;
            }
            pageable = PageRequest.of(
                    parseInt(params.getOrDefault("page", "0")),
                    parseInt(params.getOrDefault("size", "6")),
                    Sort.by(direction, "lastName", "firstName", "middleName"));
        } else {
            pageable = PageRequest.of(
                    parseInt(params.getOrDefault("page", "0")),
                    parseInt(params.getOrDefault("size", "6")),
                    sort);
        }

        OrganizationEntity organization = userDetailsRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getOrganization();

        List<OrderStatus> inactiveStatuses = Arrays.asList(ARRIVED, CANCELED);

        List<Employee> employees = getEmployees(organization, inactiveStatuses);

        return new PageImpl<>(employees, pageable, employees.size());

    }

    private static List<Employee> getEmployees(OrganizationEntity organization, List<OrderStatus> inactiveStatuses) {
        Set<UserDetailsEntity> employees = organization.getEmployees();
        Set<InvitationEntity> invitations = organization.getInvitations();
        Set<UserDetailsEntity> invitees = invitations.stream()
                .map(InvitationEntity::getInvitee)
                .collect(Collectors.toSet());
        employees.addAll(invitees);
        return employees.stream()
                .map(employee -> {
                    String name = employee.getLastName() + " " + employee.getFirstName() + " " + employee.getMiddleName();
                    List<CurrentOrder> currentOrders = getCurrentOrders(employee, inactiveStatuses, organization);
                    String position = getPosition(employee);
                    String status = "Authorized";
                    if (invitees.contains(employee)) {
                        currentOrders = null;
                        position = invitations.stream()
                                .filter(inv -> inv.getInvitee().equals(employee))
                                .findFirst()
                                .get()
                                .getPosition()
                                .getTitle();
                        status = "Invited";
                    }
                    return new Employee(
                            employee.getUserId(),
                            name,
                            employee.getEmail(),
                            currentOrders,
                            position,
                            status
                    );
                })
                .collect(Collectors.toList());
    }

    private static String getPosition(UserDetailsEntity employee) {
        return employee.getPosition().getTitle();
    }

    private static List<CurrentOrder> getCurrentOrders(UserDetailsEntity employee,
                                                       List<OrderStatus> inactiveStatuses,
                                                       OrganizationEntity organization) {

        return employee.getAcceptedOrders().stream()
                .filter(order -> !inactiveStatuses.contains(order.getStatus()))
                .map(AdMapper::toCurrentOrder)
                .toList();
    }


public String inviteEmployee(Long inviterId, InviteRequest request) {
    UserDetailsEntity inviter = userDetailsRepository.findById(inviterId)
            .orElseThrow(() -> new NotFoundException("User not found"));
    OrganizationEntity organization = inviter.getOrganization();
    Position position = positionRepository.findByOrganizationOrganizationIdAndTitle(
                    organization.getOrganizationId(), request.position())
            .orElseThrow(() -> new NotFoundException("Position not found"));
    UserDetailsEntity invitee = getInvitee(request, organization, position);
    InvitationEntity invitation = new InvitationEntity(LocalDate.now(), inviter, invitee, organization, position);
    invitationRepository.save(invitation);
    mailService.sendInvitation(request.email(), inviter.getName(), organization.getName(), position.getTitle());
    return request.email();

}

private UserDetailsEntity getInvitee(InviteRequest request, OrganizationEntity organization, Position position) {
    return userDetailsRepository.findByEmail(request.email())
            .orElseGet(() -> {
                UserEntity newUser = new UserEntity();
                newUser.setEmail(request.email());
                newUser.setInvited(true);

                UserDetailsEntity userDetails = new UserDetailsEntity();
                userDetails.setUser(newUser);
                userDetails.setEmail(request.email());
                userDetails.setOrganization(organization);
                userDetails.setPosition(position);

                return userDetailsRepository.save(userDetails);
            });
}
}
