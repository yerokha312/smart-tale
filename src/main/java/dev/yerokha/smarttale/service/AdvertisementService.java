package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.AcceptanceRequest;
import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.Card;
import dev.yerokha.smarttale.dto.CreateAdInterface;
import dev.yerokha.smarttale.dto.CreateJobRequest;
import dev.yerokha.smarttale.dto.CreateOrderRequest;
import dev.yerokha.smarttale.dto.CreateProductRequest;
import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.DashboardOrder;
import dev.yerokha.smarttale.dto.ImageOperation;
import dev.yerokha.smarttale.dto.MonitoringOrder;
import dev.yerokha.smarttale.dto.OrderDto;
import dev.yerokha.smarttale.dto.PurchaseRequest;
import dev.yerokha.smarttale.dto.PushNotification;
import dev.yerokha.smarttale.dto.SmallOrder;
import dev.yerokha.smarttale.dto.UpdateAdRequest;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.AcceptanceEntity;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.JobEntity;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.advertisement.PurchaseEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import dev.yerokha.smarttale.exception.AlreadyTakenException;
import dev.yerokha.smarttale.exception.ForbiddenException;
import dev.yerokha.smarttale.exception.MissedException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.mapper.AdMapper;
import dev.yerokha.smarttale.repository.AcceptanceRepository;
import dev.yerokha.smarttale.repository.AdvertisementRepository;
import dev.yerokha.smarttale.repository.JobRepository;
import dev.yerokha.smarttale.repository.OrderRepository;
import dev.yerokha.smarttale.repository.OrganizationRepository;
import dev.yerokha.smarttale.repository.ProductRepository;
import dev.yerokha.smarttale.repository.PurchaseRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import dev.yerokha.smarttale.util.EncryptionUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
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

import static dev.yerokha.smarttale.enums.OrderStatus.CHECKING;
import static dev.yerokha.smarttale.enums.OrderStatus.COMPLETED;
import static dev.yerokha.smarttale.enums.OrderStatus.IN_PROGRESS;
import static dev.yerokha.smarttale.enums.OrderStatus.NEW;
import static dev.yerokha.smarttale.enums.OrderStatus.PENDING;
import static dev.yerokha.smarttale.mapper.AdMapper.mapToCards;
import static dev.yerokha.smarttale.mapper.AdMapper.mapToFullCard;
import static dev.yerokha.smarttale.mapper.AdMapper.toFullDto;
import static dev.yerokha.smarttale.mapper.AdMapper.toOrderDto;
import static dev.yerokha.smarttale.mapper.CustomPageMapper.getCustomPage;
import static dev.yerokha.smarttale.service.TokenService.getOrgIdFromAuthToken;
import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;
import static java.lang.Integer.parseInt;

@Service
public class AdvertisementService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AdvertisementRepository advertisementRepository;
    private final ImageService imageService;
    private final UserService userService;
    private final MailService mailService;
    private final PurchaseRepository purchaseRepository;
    private final TaskKeyGeneratorService taskKeyGeneratorService;
    private final OrganizationRepository organizationRepository;
    private final AcceptanceRepository acceptanceRepository;
    private final UserDetailsRepository userDetailsRepository;

    private static final byte CLOSE = 1;
    private static final byte DISCLOSE = 2;
    private static final byte DELETE = 3;
    private static final byte RESTORE = 4;
    private final OrganizationService organizationService;
    private final JobRepository jobRepository;

    public AdvertisementService(ProductRepository productRepository,
                                OrderRepository orderRepository,
                                AdvertisementRepository advertisementRepository,
                                UserService userService,
                                ImageService imageService,
                                MailService mailService,
                                PurchaseRepository purchaseRepository,
                                TaskKeyGeneratorService taskKeyGeneratorService,
                                OrganizationRepository organizationRepository, AcceptanceRepository acceptanceRepository, UserDetailsRepository userDetailsRepository, OrganizationService organizationService, JobRepository jobRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.advertisementRepository = advertisementRepository;
        this.mailService = mailService;
        this.userService = userService;
        this.imageService = imageService;
        this.purchaseRepository = purchaseRepository;
        this.taskKeyGeneratorService = taskKeyGeneratorService;
        this.organizationRepository = organizationRepository;
        this.acceptanceRepository = acceptanceRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.organizationService = organizationService;
        this.jobRepository = jobRepository;
    }

    // get Ads in Personal account -> My advertisements
    public CustomPage<AdvertisementInterface> getAds(Long userId, Map<String, String> params) {
        Pageable pageable = getPageable(params);

        String query = params.get("q");

        Page<AdvertisementInterface> page;
        if (query != null) {
            page = switch (query) {
                case "orders" -> getOrders(userId, pageable);
                case "products" -> getProducts(userId, pageable);
                default -> Page.empty();
            };
        } else {
            page = getAllAds(userId, pageable);
        }

        return getCustomPage(page);
    }

    // get Ads in Personal account -> My advertisements
    private Page<AdvertisementInterface> getAllAds(Long userId, Pageable pageable) {
        return advertisementRepository
                .findAllByPublishedByUserIdAndIsDeletedFalse(userId, pageable)
                .map(AdMapper::toDto);
    }

    // get Products in Personal account -> My advertisements
    private Page<AdvertisementInterface> getProducts(Long userId, Pageable pageable) {
        return productRepository.findAllByPublishedByUserIdAndIsDeletedFalse(userId, pageable)
                .map(AdMapper::toDto);
    }

    // get Orders in Personal account -> My advertisements
    private Page<AdvertisementInterface> getOrders(Long userId, Pageable pageable) {
        return orderRepository.findAllByPublishedByUserIdAndIsDeletedFalse(userId, pageable)
                .map(AdMapper::toDto);
    }

    // get one Ad of user in Personal account -> My advertisements
    public AdvertisementInterface getAd(Long userId, Long advertisementId) {
        return toFullDto(getAdEntity(userId, advertisementId));
    }

    public String interactWithAd(Long userId, Long advertisementId, byte actionId) {
        return switch (actionId) {
            case CLOSE -> closeAd(userId, advertisementId);
            case DISCLOSE -> discloseAd(userId, advertisementId);
            case DELETE -> deleteAd(userId, advertisementId);
            case RESTORE -> restoreAd(userId, advertisementId);
            default -> throw new IllegalArgumentException("Unsupported action id");
        };
    }

    private String restoreAd(Long userId, Long advertisementId) {
        Advertisement advertisement = getAdEntity(userId, advertisementId);
        advertisement.setDeleted(false);
        advertisementRepository.save(advertisement);
        return "Ad restored";
    }

    private String deleteAd(Long userId, Long advertisementId) {
        Advertisement advertisement = getAdEntity(userId, advertisementId);
        if (advertisement instanceof OrderEntity order && order.getAcceptedBy() != null) {
            throw new ForbiddenException("You are not allowed to delete an accepted order");
        }
        advertisement.setDeleted(true);
        advertisementRepository.save(advertisement);
        return "Ad deleted";
    }

    private String discloseAd(Long userId, Long advertisementId) {
        Advertisement advertisement = getAdEntity(userId, advertisementId);
        advertisement.setClosed(false);
        advertisementRepository.save(advertisement);
        return "Ad disclosed";
    }

    private String closeAd(Long userId, Long advertisementId) {
        Advertisement advertisement = getAdEntity(userId, advertisementId);
        advertisement.setClosed(true);
        advertisementRepository.save(advertisement);
        return "Ad closed";
    }

    private Advertisement getAdEntity(Long userId, Long advertisementId) {
        return advertisementRepository.findByPublishedByUserIdAndAdvertisementIdAndIsDeletedFalse(userId, advertisementId)
                .orElseThrow(() -> new NotFoundException("Ad not found"));
    }

    public void updateAd(Long userId, UpdateAdRequest request, List<MultipartFile> files) {
        Advertisement advertisement = getAdEntity(userId, request.advertisementId());
        if (!advertisement.getPublishedBy().getUserId().equals(userId)) {
            throw new ForbiddenException("It is not your ad");
        }

        advertisement.setTitle(request.title());
        advertisement.setDescription(request.description());
        if (advertisement instanceof OrderEntity order) {
            order.setDeadlineAt(request.deadlineAt());
            order.setSize(request.size());
            order.setPrice(request.price());
        } else if (advertisement instanceof ProductEntity product) {
            product.setPrice(request.price());
        } else {
            throw new IllegalArgumentException("Unsupported advertisement type");
        }

        if (request.imageOperations() != null && !request.imageOperations().isEmpty()) {
            List<Image> images = advertisement.getImages();
            updateImages(images, files, request.imageOperations());
        }

        advertisementRepository.save(advertisement);
    }

    private void updateImages(List<Image> existingImages, List<MultipartFile> files, List<ImageOperation> imageOperationList) {
        for (ImageOperation imageOperation : imageOperationList) {
            switch (imageOperation.action()) {
                case ADD -> {
                    if (existingImages.size() >= 5) {
                        throw new IllegalArgumentException("You can not upload more than 5 images");
                    }

                    existingImages.add(imageOperation.targetPosition(), imageService.processImage(files.get(imageOperation.filePosition())));
                }
                case MOVE ->
                        Collections.swap(existingImages, imageOperation.arrayPosition(), imageOperation.targetPosition());
                case REMOVE -> existingImages.remove(imageOperation.arrayPosition());
                case REPLACE ->
                        existingImages.set(imageOperation.arrayPosition(), imageService.processImage(files.get(imageOperation.filePosition())));
            }

        }
    }

    public CustomPage<Card> getPurchases(Long userId, Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                Integer.parseInt(params.getOrDefault("page", "0")),
                Integer.parseInt(params.getOrDefault("size", "8")),
                Sort.by(Sort.Direction.DESC, "purchasedAt"));

        Page<Card> page = purchaseRepository.findAllByPurchasedByUserId(userId, pageable)
                .map(purchase -> {
                    ProductEntity product = purchase.getProduct();
                    product.setAdvertisementId(purchase.getPurchaseId());
                    return mapToCards(product);
                });
        return getCustomPage(page);
    }

    public AdvertisementInterface getPurchase(Long purchaseId) {
        PurchaseEntity purchase = purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found"));
        ProductEntity product = purchase.getProduct();

        product.setPurchasedAt(purchase.getPurchasedAt());
        product.setAdvertisementId(purchaseId);

        return mapToFullCard(product);
    }

    // in Personal account -> My orders
    public CustomPage<SmallOrder> getOrders(Long userId, Map<String, String> params) {
        Sort sort = getSortProps(params);
        if (params.get("q").equals("active")) {
            Pageable pageable = PageRequest.of(
                    Integer.parseInt(params.getOrDefault("page", "0")),
                    Integer.parseInt(params.getOrDefault("size", "12")),
                    sort);
            Page<SmallOrder> page = orderRepository.findAllByPublishedByUserIdAndAcceptedByIsNotNullAndCompletedAtIsNull(userId, pageable)
                    .map(AdMapper::toSmallOrder);
            return getCustomPage(page);
        }

        Pageable pageable = PageRequest.of(
                Integer.parseInt(params.getOrDefault("page", "0")),
                Integer.parseInt(params.getOrDefault("size", "12")),
                sort);
        Page<SmallOrder> page = orderRepository.findAllByPublishedByUserIdAndCompletedAtNotNull(userId, pageable)
                .map(AdMapper::toSmallOrder);
        return getCustomPage(page);
    }

    private Sort getSortProps(Map<String, String> params) {
        List<Sort.Order> orders = new ArrayList<>();
        params.forEach((key, value) -> {
            if (!(key.startsWith("page") || key.startsWith("size") || key.equals("q"))) {
                Sort.Direction direction = value.equalsIgnoreCase("asc") ?
                        Sort.Direction.ASC : Sort.Direction.DESC;
                orders.add(0, new Sort.Order(direction, key));
            }
        });
        return orders.isEmpty() ? Sort.by(Sort.Direction.DESC, "deadlineAt") : Sort.by(orders);
    }

    // in Personal account -> My orders
    public OrderDto getOrder(Long userId, Long orderId) {
        return toOrderDto(orderRepository.findByPublishedByUserIdAndAdvertisementId(userId, orderId)
                .orElseThrow(() -> new NotFoundException("Order not found")));
    }

    private static Pageable getPageable(Map<String, String> params) {
        return PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "10")),
                Sort.by(Sort.Direction.DESC, "publishedAt"));
    }

    private Advertisement getAdById(Long advertisementId) {
        return advertisementRepository
                .findById(advertisementId)
                .orElseThrow(() -> new NotFoundException("Advertisement not found"));
    }

    // get Ads for marketplace
    public CustomPage<Card> getAds(Map<String, String> params) {
        Pageable pageable = getPageable(params);

        String query = params.get("type");

        Page<Card> page;
        page = switch (query) {
            case "orders" -> orderRepository
                    .findAllByAcceptedByIsNullAndIsClosedFalseAndIsDeletedFalse(pageable)
                    .map(AdMapper::mapToCards);
            case "products" -> productRepository
                    .findAllByIsClosedFalseAndIsDeletedFalse(pageable)
                    .map(AdMapper::mapToCards);
            default -> Page.empty();
        };

        return getCustomPage(page);
    }

    // get Ad for marketplace
    public AdvertisementInterface getAd(Long advertisementId) {
        Advertisement advertisement = getAdById(advertisementId);
        if (advertisement.isClosed() || advertisement.isDeleted()) {
            throw new NotFoundException("Advertisement not found");
        }

        if (advertisement instanceof OrderEntity order && order.getAcceptedAt() != null) {
            throw new NotFoundException("Advertisement not found");
        }

        return mapToFullCard(advertisement);
    }

    @Transactional
    public void purchaseProduct(Long advertisementId, Long buyerId) {
        ProductEntity product = (ProductEntity) getAdById(advertisementId);

        if (product.isClosed() || product.isDeleted()) {
            throw new NotFoundException("Advertisement not found");
        }

        UserDetailsEntity buyer = userService.getUserDetailsEntity(buyerId);
        UserDetailsEntity seller = product.getPublishedBy();

        if (seller.getUserId().equals(buyerId)) {
            throw new ForbiddenException("You are not allowed to purchase your own product");
        }

        product.getPurchases().add(new PurchaseEntity(
                buyer,
                product,
                LocalDateTime.now()
        ));

        productRepository.save(product);

        sendPurchaseRequest(product, buyer, seller);
    }

    private void sendPurchaseRequest(ProductEntity product, UserDetailsEntity buyer, UserDetailsEntity seller) {
        String price = product.getPrice() == null ? "" : product.getPrice().toString();
        mailService.sendPurchaseRequest(new PurchaseRequest(
                product.getTitle(),
                product.getDescription(),
                price,
                buyer.getEmail(),
                buyer.getPhoneNumber(),
                seller.getEmail(),
                seller.getPhoneNumber()
        ));
    }

    @Transactional
    public PushNotification acceptOrder(Long advertisementId, Long userId) {
        OrderEntity order = (OrderEntity) getAdById(advertisementId);

        if (order.isClosed() || order.isDeleted()) {
            throw new NotFoundException("Advertisement not found");
        }

        if (order.getAcceptedAt() != null) {
            throw new MissedException("You are late!");
        }

        if (order.getPublishedBy().getUserId().equals(userId)) {
            throw new ForbiddenException("You are not allowed to accept your own order");
        }

        OrganizationEntity organization = organizationService.getOrganizationByEmployeeId(userId);
        if (acceptanceRepository
                .existsByOrganization_OrganizationIdAndOrder_AdvertisementId(
                        organization.getOrganizationId(), advertisementId)) {
            throw new AlreadyTakenException("An acceptance request has already been sent by this organization");
        }


        AcceptanceEntity acceptance = new AcceptanceEntity(
                order, organization, LocalDate.now()
        );

        acceptanceRepository.save(acceptance);
        order.setStatus(PENDING);
        order.addAcceptanceRequest(acceptance);
        orderRepository.save(order);

        String encryptedCode = "?code=" + EncryptionUtil.encrypt(String.valueOf(acceptance.getAcceptanceId()));
        sendAcceptanceRequest(acceptance, encryptedCode);
        String imageUrl = order.getImages() == null || order.getImages().isEmpty() ? "" : order.getImages().get(0).getImageUrl();

        Map<String, String> data = new HashMap<>();
        data.put("sub", "Запрос о принятии заказа");
        data.put("orderId", order.getAdvertisementId().toString());
        data.put("orderImg", imageUrl);
        data.put("title", order.getTitle());
        data.put("orgId", organization.getOrganizationId().toString());
        data.put("orgName", organization.getName());
        data.put("logo", organization.getImage() == null ? "" : organization.getImage().getImageUrl());
        data.put("code", encryptedCode);

        return new PushNotification(
                order.getPublishedBy().getUserId(),
                data
        );
    }

    private void sendAcceptanceRequest(AcceptanceEntity acceptance, String encryptedCode) {
        OrderEntity order = acceptance.getOrder();
        String price = order.getPrice() == null ? null : order.getPrice().toString();
        OrganizationEntity organization = acceptance.getOrganization();
        Image image = organization.getImage();
        String imageUrl = image == null ? "" : image.getImageUrl();
        AcceptanceRequest request = new AcceptanceRequest(
                order.getTitle(),
                order.getDescription(),
                price,
                organization.getOrganizationId().toString(),
                organization.getName(),
                imageUrl
        );

        mailService.sendAcceptanceRequest(order.getPublishedBy().getEmail(), request, encryptedCode);


    }

    @Transactional
    public String createAd(CreateAdInterface request, List<MultipartFile> files, Authentication authentication) {
        if (request instanceof CreateJobRequest job) {
            return createJob(job, files, getUserIdFromAuthToken(authentication), getOrgIdFromAuthToken(authentication));
        } else if (request instanceof CreateOrderRequest order) {
            return createOrder(order, files, getUserIdFromAuthToken(authentication));
        } else if (request instanceof CreateProductRequest product) {
            return createProduct(product, files, getUserIdFromAuthToken(authentication));
        }
        throw new IllegalArgumentException("Unknown advertisement type");
    }

    private String createJob(CreateJobRequest request, List<MultipartFile> files, Long userId, Long orgId) {
        UserDetailsEntity user = userDetailsRepository.getReferenceById(userId);
        OrganizationEntity organization = organizationRepository.getReferenceById(orgId);
        List<Image> images = files != null && !files.isEmpty()
                ? files.stream().map(imageService::processImage).toList()
                : Collections.emptyList();
        JobEntity job = new JobEntity(
                LocalDateTime.now(),
                user,
                request.title(),
                request.description(),
                images,
                request.contactInfo(),
                organization,
                request.jobType(),
                request.location(),
                request.salary(),
                request.applicationDeadline()
        );

        jobRepository.save(job);

        return "Job created";
    }

    private String createOrder(CreateOrderRequest request, List<MultipartFile> files, Long userId) {
        OrderEntity order = new OrderEntity();

        UserDetailsEntity user = userDetailsRepository.getReferenceById(userId);

        order.setPublishedBy(user);
        order.setTitle(request.title());
        order.setDescription(request.description());
        order.setPrice(request.price());
        order.setPublishedAt(LocalDateTime.now());
        order.setSize(request.size());
        order.setDeadlineAt(request.deadline());
        order.setContactInfo(request.contactInfo());

        if (files != null && !files.isEmpty()) {
            order.setImages(files.stream()
                    .map(imageService::processImage)
                    .toList());
        }

        orderRepository.save(order);

        return "Order created";
    }

    private String createProduct(CreateProductRequest request, List<MultipartFile> files, Long userId) {
        ProductEntity product = new ProductEntity();

        UserDetailsEntity user = userDetailsRepository.getReferenceById(userId);

        product.setPublishedBy(user);
        product.setTitle(request.title());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setPublishedAt(LocalDateTime.now());
        product.setContactInfo(request.contactInfo());

        if (files != null && !files.isEmpty()) {
            product.setImages(files.stream()
                    .map(imageService::processImage)
                    .toList());
        }

        productRepository.save(product);

        return "Product created";
    }

    @Transactional
    public PushNotification confirmOrder(String code, Long userId) {
        AcceptanceEntity acceptance = acceptanceRepository.findById(
                        Long.valueOf(EncryptionUtil.decrypt(code)))
                .orElseThrow(() -> new NotFoundException("Acceptance not found"));

        LocalDate requestDate = acceptance.getRequestedAt();

        if (requestDate.plusDays(7).isBefore(LocalDate.now())) {
            throw new MissedException("Request is expired");
        }

        OrderEntity order = acceptance.getOrder();

        if (!order.getPublishedBy().getUserId().equals(userId)) {
            throw new ForbiddenException("It is not your order");
        }

        OrganizationEntity organization = acceptance.getOrganization();

        order.setAcceptedBy(organization);
        order.setAcceptedAt(LocalDate.now());
        order.setStatus(NEW);
        order.setTaskKey(taskKeyGeneratorService.generateTaskKey(organization));
//        order.getAcceptanceEntities().clear();

        orderRepository.save(order);
        acceptanceRepository.deleteAll(order.getAcceptanceEntities());

        Map<String, String> data = new HashMap<>();
        data.put("sub", "Подтвержден заказ");
        data.put("orderId", order.getAdvertisementId().toString());
        data.put("title", order.getTitle());
        data.put("key", order.getTaskKey());
        data.put("authorId", userId.toString());
        data.put("authorName", order.getPublishedBy().getName());
        Image authorAvatar = order.getPublishedBy().getImage();
        data.put("authorAvatar", authorAvatar == null ? "" : authorAvatar.getImageUrl());
        return new PushNotification(
                organization.getOrganizationId(),
                data
        );
    }

    @Transactional
    public void incrementViewCount(Long advertisementId) {
        advertisementRepository.incrementViewsCount(advertisementId);
    }

    public List<DashboardOrder> getDashboard(Long userId) {
        UserDetailsEntity user = userService.getUserDetailsEntity(userId);
        OrganizationEntity organization = user.getOrganization();
        return orderRepository.findAllDashboardOrders(organization.getOrganizationId(), COMPLETED).stream()
                .map(AdMapper::toDashboardOrder)
                .sorted(Comparator.comparing(DashboardOrder::status))
                .toList();
    }

    public MonitoringOrder getMonitoringOrder(Long userId, Long orderId) {
        OrganizationEntity organization = userService.getUserDetailsEntity(userId).getOrganization();

        OrderEntity order = getOrderEntity(orderId);

        if (!order.getAcceptedBy().getOrganizationId().equals(organization.getOrganizationId())) {
            throw new NotFoundException("Order not found");
        }

        return AdMapper.mapToMonitoringOrder(order);
    }

    @Transactional
    public PushNotification updateStatus(Long userId, Long orderId, String status) {
        OrderEntity order = getOrderEntity(orderId);
        UserDetailsEntity user = userService.getUserDetailsEntity(userId);

        if (order.getAcceptedBy().getEmployees().stream().noneMatch(emp -> emp.getUserId().equals(userId))) {
            throw new NotFoundException("Order not found");
        }

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

        if (newStatus.equals(oldStatus)) {
            return null;
        }

        boolean isValidTransition = switch (newStatus) {
            case NEW, DISPATCHED -> oldStatus.equals(CHECKING);
            case IN_PROGRESS -> oldStatus.equals(NEW) || oldStatus.equals(CHECKING);
            case CHECKING -> oldStatus.equals(IN_PROGRESS);
            default -> false;
        };

        if (isValidTransition) {
            order.setStatus(newStatus);
        } else {
            throw new ForbiddenException(String.format("Status %s cannot be changed to status %s", oldStatus, newStatus));
        }

        orderRepository.save(order);

        String avatarUrl = user.getImage() == null ? "" : user.getImage().getImageUrl();
        Map<String, String> data = new HashMap<>();
        data.put("sub", "Статус заказа обновлен");
        data.put("employeeId", userId.toString());
        data.put("employeeName", user.getName());
        data.put("employeeAvatar", avatarUrl);
        data.put("taskId", order.getAdvertisementId().toString());
        data.put("key", order.getTaskKey());
        data.put("title", order.getTitle());
        data.put("oldStatus", oldStatus.name());
        data.put("newStatus", newStatus.name());
        return new PushNotification(
                order.getAcceptedBy().getOrganizationId(),
                data
        );
    }


    private OrderEntity getOrderEntity(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));
    }

    @Transactional
    public void deleteTask(Long userId, Long orderId) {
        OrderEntity order = getOrderEntity(orderId);
        OrganizationEntity organization = userService.getUserDetailsEntity(userId).getOrganization();
        if (order.getAcceptedBy() == null || !Objects.equals(order.getAcceptedBy().getOrganizationId(),
                organization.getOrganizationId())) {
            deleteUnacceptedOrderTask(orderId, organization);
        } else {
            List<UserDetailsEntity> contractors = order.getContractors();
            order.setAcceptedBy(null);
            order.setAcceptedAt(null);
            order.setStatus(null);
            order.setTaskKey(null);
            order.setComment(null);
            for (UserDetailsEntity contractor : contractors) {
                contractor.removeAssignedTask(order);
                userDetailsRepository.updateActiveOrdersCount(-1, contractor.getUserId());
                userDetailsRepository.save(contractor);
            }
            order.setContractors(null);
            orderRepository.save(order);
            organization.getAcceptedOrders().removeIf(o -> o.getAdvertisementId().equals(orderId));
            organizationRepository.save(organization);
        }

    }

    private void deleteUnacceptedOrderTask(Long orderId, OrganizationEntity organization) {
        AcceptanceEntity acceptance = organization.getAcceptanceEntities().stream()
                .filter(a -> a.getOrder().getAdvertisementId().equals(orderId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Acceptance not found"));

        organization.getAcceptanceEntities().remove(acceptance);
        acceptanceRepository.delete(acceptance);
    }
}