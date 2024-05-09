package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.CreateOrgRequest;
import dev.yerokha.smarttale.dto.Employee;
import dev.yerokha.smarttale.dto.EmployeeDto;
import dev.yerokha.smarttale.dto.EmployeeTasksResponse;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.dto.OrderSummary;
import dev.yerokha.smarttale.dto.Organization;
import dev.yerokha.smarttale.dto.OrganizationSummary;
import dev.yerokha.smarttale.dto.PositionSummary;
import dev.yerokha.smarttale.dto.Task;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
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
import dev.yerokha.smarttale.util.EncryptionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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


    public OrganizationService(OrderRepository orderRepository, MailService mailService, InvitationRepository invitationRepository, UserDetailsRepository userDetailsRepository, PositionRepository positionRepository, OrganizationRepository organizationRepository, ImageService imageService) {
        this.orderRepository = orderRepository;
        this.mailService = mailService;
        this.invitationRepository = invitationRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.positionRepository = positionRepository;
        this.organizationRepository = organizationRepository;
        this.imageService = imageService;
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
        String dateType = params.get("dateType");

        if (dateType != null) {
            LocalDate dateFrom = parse(params.get("dateFrom"));
            LocalDate dateTo = parse(params.get("dateTo"));
            return orderRepository.findByDateRange(organizationId, isActive, dateType, dateFrom, dateTo, pageable)
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
                orders,
                position,
                status
        );
    }

    private OrganizationEntity getOrganizationByEmployeeId(Long employeeId) {
        OrganizationEntity organization = userDetailsRepository.findById(employeeId)
                .orElseThrow(() -> new NotFoundException("User not found"))
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
            return null;
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

    public List<PositionSummary> getPositions(Long userId) {
        return userDetailsRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"))
                .getOrganization()
                .getPositions().stream()
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
        String avatarUrl = owner.getImage() == null ? null : owner.getImage().getImageUrl();
        String logoUrl = organization.getImage() == null ? null : organization.getImage().getImageUrl();
        return new Organization(
                organization.getOrganizationId(),
                organization.getName(),
                organization.getDescription(),
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
                organization.getImage() == null ? null : organization.getImage().getImageUrl()
        );
    }

    public Organization getOrganizationById(Long organizationId) {
        return mapToOrganization(organizationRepository.findById(organizationId).orElseThrow(
                () -> new NotFoundException("Organization not found")));
    }

    public void createOrganization(CreateOrgRequest request, MultipartFile file, Long userId) {
        UserDetailsEntity user = userDetailsRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found"));

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

        user.setOrganization(organization);
        organizationRepository.save(organization);
        userDetailsRepository.save(user);
    }

    public void updateOrganization(CreateOrgRequest request, MultipartFile file, Long userId) {
        UserDetailsEntity user = userDetailsRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("User not found"));

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
}