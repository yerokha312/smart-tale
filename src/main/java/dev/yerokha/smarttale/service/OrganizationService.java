package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.CreateOrgRequest;
import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.Employee;
import dev.yerokha.smarttale.dto.EmployeeDto;
import dev.yerokha.smarttale.dto.EmployeeTasksResponse;
import dev.yerokha.smarttale.dto.InviteRequest;
import dev.yerokha.smarttale.dto.InviterInvitation;
import dev.yerokha.smarttale.dto.Job;
import dev.yerokha.smarttale.dto.JobSummary;
import dev.yerokha.smarttale.dto.OrderAccepted;
import dev.yerokha.smarttale.dto.Organization;
import dev.yerokha.smarttale.dto.OrganizationSummary;
import dev.yerokha.smarttale.dto.Position;
import dev.yerokha.smarttale.dto.PositionDto;
import dev.yerokha.smarttale.dto.PositionSummary;
import dev.yerokha.smarttale.dto.Task;
import dev.yerokha.smarttale.dto.UpdateTaskRequest;
import dev.yerokha.smarttale.entity.AdvertisementImage;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.JobEntity;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.user.InvitationEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.Role;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;
import dev.yerokha.smarttale.exception.AlreadyTakenException;
import dev.yerokha.smarttale.exception.ForbiddenException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.mapper.AdMapper;
import dev.yerokha.smarttale.repository.InvitationRepository;
import dev.yerokha.smarttale.repository.JobRepository;
import dev.yerokha.smarttale.repository.OrderRepository;
import dev.yerokha.smarttale.repository.OrganizationRepository;
import dev.yerokha.smarttale.repository.PositionRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import dev.yerokha.smarttale.util.Authorities;
import dev.yerokha.smarttale.util.EncryptionUtil;
import org.hibernate.Hibernate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static dev.yerokha.smarttale.mapper.CustomPageMapper.getCustomPage;
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
    private final AdMapper adMapper;
    private final JobRepository jobRepository;
    private final PushNotificationService pushNotificationService;


    public OrganizationService(OrderRepository orderRepository,
                               MailService mailService,
                               InvitationRepository invitationRepository,
                               UserDetailsRepository userDetailsRepository,
                               PositionRepository positionRepository,
                               OrganizationRepository organizationRepository,
                               ImageService imageService,
                               AuthenticationService authenticationService, AdMapper adMapper, JobRepository jobRepository, PushNotificationService pushNotificationService) {
        this.orderRepository = orderRepository;
        this.mailService = mailService;
        this.invitationRepository = invitationRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.positionRepository = positionRepository;
        this.organizationRepository = organizationRepository;
        this.imageService = imageService;
        this.authenticationService = authenticationService;
        this.adMapper = adMapper;
        this.jobRepository = jobRepository;
        this.pushNotificationService = pushNotificationService;
    }

    public CustomPage<OrderAccepted> getOrders(Long organizationId, Map<String, String> params) {
        Sort sort = getSortProps(params);
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "6")),
                sort.equals(Sort.unsorted()) ?
                        Sort.by(Sort.Direction.DESC, "acceptedAt") : sort);

        boolean isActive = Boolean.parseBoolean(params.getOrDefault("active", "true"));
        String dateType = params.get("dateType");

        if (dateType != null) {
            LocalDate dateFrom = parse(params.get("dateFrom"));
            LocalDate dateTo = parse(params.get("dateTo"));
            Page<OrderAccepted> page = orderRepository.findByDateRange(
                    organizationId, isActive, dateType, dateFrom, dateTo, pageable);
            return getCustomPage(page);
        }

        Page<OrderAccepted> page = orderRepository.findByActiveStatus(organizationId, isActive, pageable);
        return getCustomPage(page);
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

    public CustomPage<Employee> getEmployees(Long organizationId, Map<String, String> params) {
        Sort sort = getSortProps(params);

        if (sort.isUnsorted()) {
            sort = Sort.by(Sort.Direction.ASC, "lastName", "firstName", "middleName");
        }

        Pageable pageable = getPageable(params, sort);

        Page<Employee> page = getEmployees(organizationId, pageable)
                .map(emp -> mapToEmployee(emp, organizationId));
        return getCustomPage(page);
    }

    private Employee mapToEmployee(UserDetailsEntity user, Long organizationId) {
        String name = user.getName();
        List<OrderAccepted> orders = getCurrentOrders(user.getUserId(), organizationId);
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

    public OrganizationEntity getOrganizationByEmployeeId(Long employeeId) {
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

    public EmployeeTasksResponse getEmployee(Long organizationId, Long employeeId, Map<String, String> params) {
        UserDetailsEntity employee = getEmployeeById(organizationId, employeeId);
        return new EmployeeTasksResponse(
                mapToEmployeeDto(organizationId, employee),
                getTasks(employee, organizationId, params)
        );
    }

    private UserDetailsEntity getEmployeeById(Long organizationId, Long employeeId) {
        return userDetailsRepository.findEmployeeById(organizationId, employeeId)
                .orElseGet(() -> getInvitedEmployee(organizationId, employeeId));
    }

    private UserDetailsEntity getInvitedEmployee(Long organizationId, Long employeeId) {
        return invitationRepository.findInviteeById(organizationId, employeeId)
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
                .map(adMapper::toTask);
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

    private List<OrderAccepted> getCurrentOrders(Long employeeId,
                                                 Long organizationId) {

        boolean isEmployee = userDetailsRepository.existsInOrganization(employeeId, organizationId);
        if (!isEmployee) {
            return null;
        }
        return orderRepository.findCurrentOrdersByEmployeeId(employeeId);
    }

    public void inviteEmployee(Long inviterId, InviteRequest request) {
        UserDetailsEntity inviter = getUserDetailsEntity(inviterId);

        OrganizationEntity organization = inviter.getOrganization();
        PositionEntity position = positionRepository.findByOrganizationOrganizationIdAndPositionId(
                        organization.getOrganizationId(), request.positionId())
                .orElseThrow(() -> new NotFoundException("Position not found"));

        UserDetailsEntity invitee = getInvitee(request);

        Optional<InvitationEntity> existingInvitationOpt = invitationRepository
                .findByInviteeIdAndOrganizationId(invitee.getUserId(), organization.getOrganizationId());

        InvitationEntity invitation;
        if (existingInvitationOpt.isPresent()) {
            invitation = existingInvitationOpt.get();
            invitation.setInvitedAt(LocalDateTime.now());
            invitation.setInviter(inviter);
            invitation.setPosition(position);
        } else {
            invitation = new InvitationEntity(LocalDateTime.now(), inviter, invitee, organization, position);
        }

        invitationRepository.save(invitation);

        String name = invitee.getName();
        Long invitationId = invitation.getInvitationId();
        String code = EncryptionUtil.encrypt(String.valueOf(invitationId));
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
        data.put("sub", "Приглашение в организацию");
        data.put("orgId", organization.getOrganizationId().toString());
        data.put("orgName", organization.getName());
        data.put("logo", organization.getImage() == null ? "" : organization.getImage().getImageUrl());
        data.put("invId", invitationId.toString());

        pushNotificationService.sendToUser(invitee.getUserId(), data);
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

                    try {
                        return userDetailsRepository.save(userDetails);
                    } catch (Exception e) {
                        throw new AlreadyTakenException("Phone number already taken");
                    }
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

    public CustomPage<OrganizationSummary> getAllOrganizations(Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                Integer.parseInt(params.getOrDefault("page", "0")),
                Integer.parseInt(params.getOrDefault("size", "10")));
        Page<OrganizationSummary> page = organizationRepository.findAll(pageable).map(OrganizationService::mapToOrganizationSummary);
        return getCustomPage(page);
    }

    private static OrganizationSummary mapToOrganizationSummary(OrganizationEntity organization) {
        return new OrganizationSummary(
                organization.getOrganizationId(),
                organization.getName(),
                organization.getImage() == null ? "" : organization.getImage().getImageUrl()
        );
    }

    public Organization getOrganizationById(Long organizationId) {
        OrganizationEntity organizationEntity = getOrganizationEntity(organizationId);
        return mapToOrganization(organizationEntity);
    }

    OrganizationEntity getOrganizationEntity(Long organizationId) {
        return organizationRepository.findById(organizationId).orElseThrow(
                () -> new NotFoundException("Organization not found"));
    }

    @Transactional
    public String createOrganization(CreateOrgRequest request, MultipartFile file, Long userId) {
        UserDetailsEntity user = userDetailsRepository.getReferenceById(userId);
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

    public void updateOrganization(CreateOrgRequest request, MultipartFile file, Long orgId) {
        OrganizationEntity organization = getOrganizationEntity(orgId);

        organization.setName(request.name());
        organization.setDescription(request.description());
        organization.setImage(imageService.processImage(file));

        organizationRepository.save(organization);
    }

    public void createPosition(Long organizationId, Position position) {
        if (!organizationId.equals(position.organizationId())) {
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
        OrganizationEntity organization = organizationRepository.getReferenceById(organizationId);
        PositionEntity positionEntity = new PositionEntity(
                position.title(),
                position.hierarchy(),
                authorities,
                organization
        );

        positionRepository.save(positionEntity);
    }

    @Transactional
    public List<String> updatePosition(Long organizationId, Position position) {
        Long positionId = position.positionId();
        boolean positionExists = positionRepository
                .existsByOrganizationOrganizationIdAndPositionId(organizationId, positionId);
        PositionEntity positionEntity;
        if (positionExists) {
            positionEntity = positionRepository.getReferenceById(positionId);
        } else {
            throw new NotFoundException("Position not found");
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

        positionEntity.setTitle(position.title());
        positionEntity.setHierarchy(hierarchy);
        positionEntity.setAuthorities(authorities);
        positionRepository.save(positionEntity);

        List<Long> employeeIdListByPosition = userDetailsRepository.findEmployeeIdsByPositionId(positionId);
        for (Long employeeId : employeeIdListByPosition) {
            Map<String, String> data = Map.of(
                    "sub", "Ваша должность обновлена",
                    "posId", positionId.toString(),
                    "title", position.title()
            );
            pushNotificationService.sendToUser(employeeId, data);
        }

        Map<String, String> data = Map.of(
                "sub", "Должность была обновлена",
                "posId", positionId.toString(),
                "title", position.title()
        );
        pushNotificationService.sendToOrganization(organizationId, data);

        return userDetailsRepository.findEmployeeEmailsByPositionId(positionId);
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

        if (!request.addedEmployees().isEmpty()) {
            assignContractors(request, organization, order);
        }

        if (!request.removedEmployees().isEmpty()) {
            removeContractors(request, organization, order);
        }

        if (request.comment() != null && !request.comment().isEmpty()) {
            order.setComment(request.comment());
        }

        orderRepository.save(order);
    }

    public void removeContractors(UpdateTaskRequest request, OrganizationEntity organization, OrderEntity order) {
        List<UserDetailsEntity> contractors = userDetailsRepository.findAllByOrganizationOrganizationIdAndUserIdIn(
                organization.getOrganizationId(), request.removedEmployees());

        for (UserDetailsEntity contractor : contractors) {
            order.removeContractor(contractor);
            contractor.removeAssignedTask(order);
            userDetailsRepository.updateActiveOrdersCount(-1, contractor.getUserId());
            userDetailsRepository.save(contractor);
            String imageUrl = order.getAdvertisementImages().stream()
                    .filter(ai -> ai.getIndex() == 0)
                    .map(AdvertisementImage::getImage)
                    .map(Image::getImageUrl)
                    .findFirst()
                    .orElse("");
            Map<String, String> data = Map.of(
                    "sub", "Вас отстранили от заказа",
                    "orderId", order.getAdvertisementId().toString(),
                    "title", order.getTitle(),
                    "key", order.getTaskKey(),
                    "image", imageUrl,
                    "status", order.getStatus().name()
            );

            pushNotificationService.sendToUser(contractor.getUserId(), data);
        }

    }

    public void assignContractors(UpdateTaskRequest request, OrganizationEntity organization, OrderEntity order) {
        List<UserDetailsEntity> contractors = userDetailsRepository.findAllByOrganizationOrganizationIdAndUserIdIn(
                organization.getOrganizationId(), request.addedEmployees());

        for (UserDetailsEntity contractor : contractors) {
            order.addContractor(contractor);
            contractor.addAssignedTask(order);
            userDetailsRepository.updateActiveOrdersCount(1, contractor.getUserId());
            userDetailsRepository.save(contractor);
            String imageUrl = order.getAdvertisementImages().stream()
                    .filter(ai -> ai.getIndex() == 0)
                    .map(AdvertisementImage::getImage)
                    .map(Image::getImageUrl)
                    .findFirst()
                    .orElse("");
            Map<String, String> data = Map.of(
                    "sub", "Вас назначили на заказ",
                    "orderId", order.getAdvertisementId().toString(),
                    "title", order.getTitle(),
                    "key", order.getTaskKey(),
                    "image", imageUrl,
                    "status", order.getStatus().name()
            );
            pushNotificationService.sendToUser(contractor.getUserId(), data);
        }

    }

    public List<PositionSummary> getAllPositions(Long organizationId) {
        return getOrganizationEntity(organizationId).getPositions().stream()
                .map(pos -> new PositionSummary(
                        pos.getPositionId(),
                        pos.getTitle()
                ))
                .toList();
    }

    public PositionDto getOnePosition(Long organizationId, Long positionId) {
        PositionEntity position = positionRepository.findByOrganizationOrganizationIdAndPositionId(
                        organizationId, positionId)
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
    public String deleteEmployee(Long organizationId, Long employeeId) {
        UserDetailsEntity employee = userDetailsRepository.findEmployeeById(organizationId, employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        employee.setOrganization(null);
        employee.setPosition(null);
        employee.setAssignedTasks(null);
        employee.setActiveOrdersCount(0);
        Set<Role> roles = employee.getUser().getAuthorities();
        roles.stream()
                .filter(role -> role.getAuthority().equals("EMPLOYEE"))
                .findFirst().ifPresent(roles::remove);

        userDetailsRepository.save(employee);

        Map<String, String> data = Map.of(
                "sub", "Вас исключили из организации",
                "email", employee.getEmail()
        );

        pushNotificationService.sendToUser(employee.getUserId(), data);

        return employee.getEmail();
    }

    public void deletePosition(Long organizationId, Long positionId) {
        PositionEntity position = positionRepository
                .findByOrganizationOrganizationIdAndPositionId(organizationId, positionId)
                .orElseThrow(() -> new NotFoundException("Position not found"));

        if (Hibernate.size(position.getEmployees()) > 0) {
            throw new ForbiddenException("Reassign user positions first");
        }

        positionRepository.delete(position);
    }

    @Transactional
    public String updateEmployee(Long organizationId, Long employeeId, Long positionId) {
        UserDetailsEntity employee = userDetailsRepository.findEmployeeById(organizationId, employeeId)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
        PositionEntity position = positionRepository.findByOrganizationOrganizationIdAndPositionId(organizationId, positionId)
                .orElseThrow(() -> new NotFoundException("Position not found"));

        employee.setPosition(position);

        userDetailsRepository.save(employee);

        Map<String, String> data = Map.of(
                "sub", "Вы назначены на новую должность",
                "posId", positionId.toString(),
                "title", position.getTitle(),
                "email", employee.getEmail()
        );

        pushNotificationService.sendToUser(employee.getUserId(), data);

        return employee.getEmail();
    }

    public CustomPage<InviterInvitation> getInvitations(Long orgId, int page, int size) {
        Page<InviterInvitation> invitationPage = invitationRepository
                .findAllByOrganizationId(orgId, PageRequest.of(page, size));
        return getCustomPage(invitationPage);
    }

    public void deleteInvitation(Long orgId, Long invId) {
        boolean belongsToOrganization = invitationRepository
                .existsByInvitationIdAndOrganizationOrganizationId(invId, orgId);
        if (belongsToOrganization) {
            invitationRepository.deleteById(invId);
        } else {
            throw new NotFoundException("Invitation not found");
        }
    }

    public CustomPage<JobSummary> getJobAds(Long orgId, Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "5")),
                Sort.Direction.DESC, "publishedAt"
        );

        Page<JobSummary> jobSummaryPage = jobRepository.findAllByOrganizationId(orgId, pageable);

        return getCustomPage(jobSummaryPage);
    }

    public Job getOneJobAd(Long orgId, Long jobId) {
        JobEntity job = getJobEntity(orgId, jobId);
        return adMapper.mapToJob(job);
    }

    JobEntity getJobEntity(Long orgId, Long jobId) {
        return jobRepository.findByOrganization_OrganizationIdAndAdvertisementIdAndIsDeletedFalse(orgId, jobId)
                .orElseThrow(() -> new NotFoundException("Job not found"));
    }

}