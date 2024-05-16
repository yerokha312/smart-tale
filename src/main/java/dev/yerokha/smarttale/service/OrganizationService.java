package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.CreateOrgRequest;
import dev.yerokha.smarttale.dto.Employee;
import dev.yerokha.smarttale.dto.EmployeeDto;
import dev.yerokha.smarttale.dto.EmployeeTasksResponse;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.dto.OrderSummary;
import dev.yerokha.smarttale.dto.Organization;
import dev.yerokha.smarttale.dto.OrganizationSummary;
import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.dto.PositionDto;
import dev.yerokha.smarttale.dto.PositionSummary;
import dev.yerokha.smarttale.dto.PushNotification;
import dev.yerokha.smarttale.dto.Task;
import dev.yerokha.smarttale.dto.UpdateTaskRequest;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.Role;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.exception.ForbiddenException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.mapper.AdMapper;
import dev.yerokha.smarttale.repository.InvitationRepository;
import dev.yerokha.smarttale.repository.OrderRepository;
import dev.yerokha.smarttale.repository.OrganizationRepository;
import dev.yerokha.smarttale.repository.PositionRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import dev.yerokha.smarttale.util.Authorities;
import dev.yerokha.smarttale.util.EncryptionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.lang.Integer.parseInt;
import static java.time.LocalDate.parse;

@Service
public class OrganizationService {

    private static final String REG_PAGE = "";
    private static final String LOGIN_PAGE = "";
    private final OrderRepository orderRepository;
    private final MailService mailService;
    private final InvitationRepository invitationRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final PositionRepository positionRepository;
    private final OrganizationRepository organizationRepository;
    private final ImageService imageService;
    private final AuthenticationService authenticationService;


    public OrganizationService(OrderRepository orderRepository,
                               MailService mailService,
                               InvitationRepository invitationRepository,
                               UserDetailsRepository userDetailsRepository,
                               PositionRepository positionRepository,
                               OrganizationRepository organizationRepository,
                               ImageService imageService,
                               AuthenticationService authenticationService) {
        this.orderRepository = orderRepository;
        this.mailService = mailService;
        this.invitationRepository = invitationRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.positionRepository = positionRepository;
        this.organizationRepository = organizationRepository;
        this.imageService = imageService;
        this.authenticationService = authenticationService;
    }

    public Page<OrderSummary> getOrders(Long employeeId, Map<String, String> params) {
        Sort sort = getSortProps(params);
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "6")),
                sort.equals(Sort.unsorted()) ?
                        Sort.by(Sort.Direction.DESC, "acceptedAt") : sort);

        Long organizationId = getUserDetailsEntity(employeeId)
                .getOrganization()
                .getOrganizationId();

        boolean isActive = Boolean.parseBoolean(params.getOrDefault("active", "true"));
        String dateType = params.get("dateType");

        if (dateType != null) {
            LocalDate dateFrom = parse(params.get("dateFrom"));
            LocalDate dateTo = parse(params.get("dateTo"));
            return orderRepository.findByDateRange(organizationId,
                            isActive,
                            dateType,
                            dateFrom,
                            dateTo,
                            pageable)
                    .map(AdMapper::toCurrentOrder);
        }

        return orderRepository.findByActiveStatus(organizationId, isActive, pageable)
                .map(AdMapper::toCurrentOrder);
    }


    private Sort getSortProps(Map<String, String> params) {
        List<Sort.Order> orders = new ArrayList<>();
        params.forEach((key, value) -> {
            if (!(key.startsWith("page") || key.startsWith("size") || key.equals("active") || key.startsWith("date"))) {
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

        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Direction.ASC, "lastName", "firstName", "middleName");
        }

        Pageable pageable = getPageable(params, sort);

        OrganizationEntity organization = getOrganizationByEmployeeId(employeeId);

        return getEmployees(organization.getOrganizationId(), pageable)
                .map(emp -> mapToEmployee(emp, organization.getOrganizationId()));
    }

    private Employee mapToEmployee(UserDetailsEntity user, Long organizationId) {
        String name = user.getName() == null ? "" : user.getName();
        List<OrderSummary> orders = getCurrentOrders(user, organizationId);
        String position = getPosition(user, organizationId);
        String status = orders == null || name.isEmpty() ? "Invited" : "Authorized";

        return new Employee(
                user.getUserId(),
                name,
                user.getEmail(),
                orders == null ? Collections.emptyList() : orders,
                position,
                status
        );
    }

    private OrganizationEntity getOrganizationByEmployeeId(Long employeeId) {
        OrganizationEntity organization = getUserDetailsEntity(employeeId)
                .getOrganization();

        if (organization == null) {
            throw new NotFoundException("Organization not found");
        }

        return organization;
    }

    private Pageable getPageable(Map<String, String> params, Sort sort) {
        return PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "10")),
                sort);
    }

    private Page<UserDetailsEntity> getEmployees(Long organizationId, Pageable pageable) {
        return userDetailsRepository
                .findAllEmployeesAndInvitees(organizationId, pageable);
    }

    public EmployeeTasksResponse getEmployee(Long userId, Long employeeId, Map<String, String> params) {
        OrganizationEntity organization = getOrganizationByEmployeeId(userId);

        UserDetailsEntity employee = findEmployeeById(organization, employeeId);
        return new EmployeeTasksResponse(
                mapToEmployeeDto(organization.getOrganizationId(), employee),
                getTasks(employee, organization.getOrganizationId(), params)
        );
    }

    private UserDetailsEntity findEmployeeById(OrganizationEntity organization, Long employeeId) {
        return organization.getEmployees().stream()
                .filter(emp -> emp.getUserId().equals(employeeId))
                .findFirst()
                .orElseGet(() -> findInvitedEmployee(organization, employeeId));
    }

    private UserDetailsEntity findInvitedEmployee(OrganizationEntity organization, Long employeeId) {
        return organization.getInvitations().stream()
                .map(InvitationEntity::getInvitee)
                .filter(invitee -> invitee.getUserId().equals(employeeId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }

    private EmployeeDto mapToEmployeeDto(Long organizationId, UserDetailsEntity employee) {
        return new EmployeeDto(
                employee.getUserId(),
                employee.getName(),
                employee.getImage() == null ? null : employee.getImage().getImageUrl(),
                employee.getEmail(),
                employee.getPhoneNumber(),
                getPosition(employee, organizationId)
        );
    }

    private Page<Task> getTasks(UserDetailsEntity employee, Long organizationId, Map<String, String> params) {
        OrganizationEntity organization = employee.getOrganization();

        if ((organization == null) || !organization.getOrganizationId().equals(organizationId)) {
            return Page.empty();
        }

        boolean isActive = Boolean.parseBoolean(params.getOrDefault("active", "true"));

        Sort sort = isActive
                ? Sort.by(Sort.Direction.DESC, "acceptedAt")
                : Sort.by(Sort.Direction.DESC, "completedAt");

        Pageable pageable = getPageable(params, sort);

        return orderRepository.findTasksByEmployeeId(employee.getUserId(), organizationId, isActive, pageable)
                .map(AdMapper::toTask);
    }

    private String getPosition(UserDetailsEntity employee, Long organizationId) {
        if (employee.getOrganization() == null || !employee.getOrganization().getOrganizationId().equals(organizationId)) {
            return Objects.requireNonNull(employee.getInvitations().stream()
                            .filter(inv -> inv.getOrganization().getOrganizationId().equals(organizationId))
                            .findFirst()
                            .orElse(null))
                    .getPosition()
                    .getTitle();
        }

        return employee.getPosition().getTitle();
    }

    private List<OrderSummary> getCurrentOrders(UserDetailsEntity employee,
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

    public PushNotification inviteEmployee(Long inviterId, InviteRequest request) {
        UserDetailsEntity inviter = getUserDetailsEntity(inviterId);

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
                inviter.getName(),
                organization,
                position.getTitle(),
                link);

        Map<String, String> data = new HashMap<>();
        data.put("email", invitee.getEmail());
        data.put("sub", "Приглашение на работу");
        data.put("id", organization.getOrganizationId().toString());
        data.put("name", organization.getName());
        data.put("logo", organization.getImage() == null ? "" : organization.getImage().getImageUrl());
//        data.put("code", encryptedCode);

        return new PushNotification(
                invitee.getUserId(),
                data
        );
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
                    userDetails.setPhoneNumber(request.phoneNumber());

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

    public List<PositionSummary> getPositionsDropdown(Long userId) {
        UserDetailsEntity user = getUserDetailsEntity(userId);
        return user
                .getOrganization()
                .getPositions().stream()
                .filter(pos -> {
                    int positionAuths = pos.getAuthorities();
                    PositionEntity userPosition = user.getPosition();
                    int userAuths = userPosition.getAuthorities();
                    return pos.getHierarchy() > userPosition.getHierarchy()
                            && ((positionAuths & userAuths) == positionAuths);
                })
                .sorted(Comparator.comparing(PositionEntity::getTitle))
                .map(pos -> new PositionSummary(
                        pos.getPositionId(),
                        pos.getTitle()
                ))
                .toList();
    }

    public Organization getOrganization(Long userId) {
        OrganizationEntity organization = getOrganizationByEmployeeId(userId);

        return mapToOrganization(organization);
    }

    private static Organization mapToOrganization(OrganizationEntity organization) {
        UserDetailsEntity owner = organization.getOwner();
        String avatarUrl = owner.getImage() == null ? "" : owner.getImage().getImageUrl();
        String logoUrl = organization.getImage() == null ? "" : organization.getImage().getImageUrl();
        return new Organization(
                organization.getOrganizationId(),
                organization.getName(),
                organization.getDescription() == null ? "" : organization.getDescription(),
                logoUrl,
                owner.getUserId(),
                owner.getName(),
                avatarUrl,
                organization.getRegisteredAt()
        );
    }

    public Page<OrganizationSummary> getAllOrganizations(Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                Integer.parseInt(params.getOrDefault("page", "0")),
                Integer.parseInt(params.getOrDefault("size", "10")));
        return organizationRepository.findAll(pageable).map(OrganizationService::mapToOrganizationSummary);
    }

    private static OrganizationSummary mapToOrganizationSummary(OrganizationEntity organization) {
        return new OrganizationSummary(
                organization.getOrganizationId(),
                organization.getName(),
                organization.getImage() == null ? "" : organization.getImage().getImageUrl()
        );
    }

    public Organization getOrganizationById(Long organizationId) {
        return mapToOrganization(organizationRepository.findById(organizationId).orElseThrow(
                () -> new NotFoundException("Organization not found")));
    }

    @Transactional
    public String createOrganization(CreateOrgRequest request, MultipartFile file, Long userId) {
        UserDetailsEntity user = getUserDetailsEntity(userId);
        if (!user.isSubscribed() || user.getOrganization() != null) {
            throw new ForbiddenException("User is not subscribed or already has an organization");
        }
        OrganizationEntity organization = new OrganizationEntity(
                request.name(),
                request.description(),
                file == null ? null : imageService.processImage(file),
                LocalDate.now(),
                user
        );
        organizationRepository.save(organization);
        PositionEntity position = new PositionEntity(
                "Owner", 0, Authorities.allAuthorities(), organization
        );
        positionRepository.save(position);
        user.setPosition(position);
        user.setOrganization(organization);
        user.getUser().setAuthorities(authenticationService.getUserAndEmployeeRole());
        userDetailsRepository.save(user);
        return user.getEmail();
    }

    public void updateOrganization(CreateOrgRequest request, MultipartFile file, Long userId) {
        UserDetailsEntity user = getUserDetailsEntity(userId);

        OrganizationEntity organization = user.getOrganization();
        boolean isOwner = organization.getOwner().getUserId().equals(userId);

        if (!isOwner) {
            throw new ForbiddenException("User is not an owner of the organization");
        }

        organization.setName(request.name());
        organization.setDescription(request.description());
        organization.setImage(imageService.processImage(file));

        organizationRepository.save(organization);
    }

    public void createPosition(Long userId, Position position) {
        UserDetailsEntity user = getUserDetailsEntity(userId);

        OrganizationEntity organization = user.getOrganization();

        if (!organization.getOrganizationId().equals(position.organizationId())) {
            throw new ForbiddenException("It is not your organization");
        }

        int authorities = 0;

        List<String> authoritiesList = position.authorities();

        for (String authority : authoritiesList) {
            authorities |= Authorities.valueOf(authority).getBitmask();
        }

        if (((authorities & Authorities.UPDATE_POSITION.getBitmask()) > 0
                || ((authorities & Authorities.INVITE_EMPLOYEE.getBitmask()) > 0)
                && (authorities & Authorities.CREATE_POSITION.getBitmask()) <= 0)) {
            throw new ForbiddenException("If were chosen either update or invite, you have to choose create");
        }

        PositionEntity positionEntity = new PositionEntity(
                position.title(),
                position.hierarchy(),
                authorities,
                organization
        );

        positionRepository.save(positionEntity);
    }

    public PositionEntity updatePosition(Long userId, Position position) {
        PositionEntity positionEntity = positionRepository.findById(position.positionId())
                .orElseThrow(() -> new NotFoundException("Position not found"));
        boolean userIsEmployeeOfPositionOrganization = positionEntity.getOrganization().getEmployees().stream()
                .anyMatch(emp -> emp.getUserId().equals(userId));
        if (!userIsEmployeeOfPositionOrganization) {
            throw new ForbiddenException("The user is not an employee of the organization associated with this position");
        }
        int hierarchy = position.hierarchy();
        int authorities = 0;
        List<String> authoritiesList = position.authorities();

        for (String authority : authoritiesList) {
            authorities |= Authorities.valueOf(authority).getBitmask();
        }

        if (((authorities & Authorities.UPDATE_POSITION.getBitmask()) > 0
                || ((authorities & Authorities.INVITE_EMPLOYEE.getBitmask()) > 0)
                && (authorities & Authorities.CREATE_POSITION.getBitmask()) == 0)) {
            throw new ForbiddenException("If were chosen either update or invite, you have to choose create");
        }

        positionEntity.setHierarchy(hierarchy);
        positionEntity.setAuthorities(authorities);

        return positionRepository.save(positionEntity);
    }

    private UserDetailsEntity getUserDetailsEntity(Long userId) {
        return userDetailsRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional
    public void updateTask(Long userId, UpdateTaskRequest request) {
        OrganizationEntity organization = getOrganizationByEmployeeId(userId);
        OrderEntity order = orderRepository.findByAcceptedByOrganizationIdAndCompletedAtIsNullAndAdvertisementId(
                        organization.getOrganizationId(), request.taskId())
                .orElseThrow(() -> new NotFoundException("Task not found"));

        if (!request.addEmployees().isEmpty()) {
            List<UserDetailsEntity> contractors = userDetailsRepository.findAllByOrganizationOrganizationIdAndUserIdIn(
                    organization.getOrganizationId(), request.addEmployees());

            for (UserDetailsEntity contractor : contractors) {
                order.addContractor(contractor);
                contractor.addAssignedTask(order);
                userDetailsRepository.updateActiveOrdersCount(1, contractor.getUserId());
                userDetailsRepository.save(contractor);
            }
        }

        if (!request.removeEmployees().isEmpty()) {
            List<UserDetailsEntity> contractors = userDetailsRepository.findAllByOrganizationOrganizationIdAndUserIdIn(
                    organization.getOrganizationId(), request.removeEmployees());

            for (UserDetailsEntity contractor : contractors) {
                order.removeContractor(contractor);
                contractor.removeAssignedTask(order);
                userDetailsRepository.updateActiveOrdersCount(-1, contractor.getUserId());
                userDetailsRepository.save(contractor);
            }
        }

        if (request.comment() != null && !request.comment().isEmpty()) {
            order.setComment(request.comment());
        }
        orderRepository.save(order);
    }

    public List<PositionSummary> getAllPositions(Long userId) {
        return getUserDetailsEntity(userId).getOrganization().getPositions().stream()
                .map(pos -> new PositionSummary(
                        pos.getPositionId(),
                        pos.getTitle()
                ))
                .toList();
    }

    public PositionDto getOnePosition(Long userId, Long positionId) {
        PositionEntity position = positionRepository.findByOrganizationOrganizationIdAndPositionId(
                        getUserDetailsEntity(userId).getOrganization().getOrganizationId(), positionId)
                .orElseThrow(() -> new NotFoundException("Position not found"));

        List<String> authorities = Authorities.getNamesByValues(position.getAuthorities());

        return new PositionDto(
                positionId,
                position.getTitle(),
                position.getHierarchy(),
                authorities
        );
    }

    @Transactional
    public String deleteEmployee(Long userId, Long employeeId) {
        OrganizationEntity organization = getOrganizationByEmployeeId(userId);

        Set<UserDetailsEntity> employees = organization.getEmployees();
        UserDetailsEntity employee = employees.stream()
                .filter(e -> e.getUserId().equals(employeeId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Employee not found"));

        employees.remove(employee);
        employee.setOrganization(null);
        employee.setPosition(null);
        employee.setAssignedTasks(null);
        employee.setActiveOrdersCount(0);
        Set<Role> roles = employee.getUser().getAuthorities();
        roles.stream()
                .filter(role -> role.getAuthority().equals("EMPLOYEE"))
                .findFirst().ifPresent(roles::remove);

        userDetailsRepository.save(employee);

        return employee.getEmail();
    }

    public void deletePosition(Long userId, Long positionId) {
        OrganizationEntity organization = getOrganizationByEmployeeId(userId);
        PositionEntity position = organization.getPositions().stream()
                .filter(p -> p.getPositionId().equals(positionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Position not found"));

        if (!position.getEmployees().isEmpty()) {
            throw new ForbiddenException("Reassign user positions first");
        }

        positionRepository.delete(position);
    }

    public String updateEmployee(Long userId, Long employeeId, Long positionId) {
        OrganizationEntity organization = getOrganizationByEmployeeId(userId);
        UserDetailsEntity employee = organization.getEmployees().stream()
                .filter(e -> e.getUserId().equals(employeeId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        employee.setPosition(organization.getPositions().stream()
                .filter(p -> p.getPositionId().equals(positionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Position not found")));

        userDetailsRepository.save(employee);

        return employee.getEmail();
    }
}