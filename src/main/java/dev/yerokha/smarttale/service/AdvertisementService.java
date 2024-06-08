package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.AcceptanceRequestMail;
import dev.yerokha.smarttale.dto.AdvertisementDto;
import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.Card;
import dev.yerokha.smarttale.dto.CreateAdInterface;
import dev.yerokha.smarttale.dto.CreateJobRequest;
import dev.yerokha.smarttale.dto.CreateOrderRequest;
import dev.yerokha.smarttale.dto.CreateProductRequest;
import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.ImageOperation;
import dev.yerokha.smarttale.dto.MonitoringOrder;
import dev.yerokha.smarttale.dto.OrderDashboard;
import dev.yerokha.smarttale.dto.OrderDto;
import dev.yerokha.smarttale.dto.Purchase;
import dev.yerokha.smarttale.dto.PurchaseRequest;
import dev.yerokha.smarttale.dto.PurchaseSummary;
import dev.yerokha.smarttale.dto.SmallOrder;
import dev.yerokha.smarttale.dto.UpdateAdRequest;
import dev.yerokha.smarttale.dto.UpdateJobRequest;
import dev.yerokha.smarttale.entity.AdvertisementImage;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.AcceptanceEntity;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.JobApplicationEntity;
import dev.yerokha.smarttale.entity.advertisement.JobEntity;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.advertisement.PurchaseEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.ApplicationStatus;
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
import dev.yerokha.smarttale.repository.PositionRepository;
import dev.yerokha.smarttale.repository.ProductRepository;
import dev.yerokha.smarttale.repository.PurchaseRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import dev.yerokha.smarttale.util.Authorities;
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
import java.util.stream.IntStream;

import static dev.yerokha.smarttale.enums.OrderStatus.CHECKING;
import static dev.yerokha.smarttale.enums.OrderStatus.COMPLETED;
import static dev.yerokha.smarttale.enums.OrderStatus.IN_PROGRESS;
import static dev.yerokha.smarttale.enums.OrderStatus.NEW;
import static dev.yerokha.smarttale.enums.OrderStatus.PENDING;
import static dev.yerokha.smarttale.mapper.CustomPageMapper.getCustomPage;
import static dev.yerokha.smarttale.service.TokenService.getOrgIdFromAuthToken;
import static dev.yerokha.smarttale.service.TokenService.getUserAuthoritiesFromToken;
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
    private final AdMapper adMapper;
    private static final byte CLOSE = 1;
    private static final byte DISCLOSE = 2;
    private static final byte DELETE = 3;
    private final OrganizationService organizationService;
    private final JobRepository jobRepository;
    private final PositionRepository positionRepository;
    private final PushNotificationService pushNotificationService;

    public AdvertisementService(ProductRepository productRepository,
                                OrderRepository orderRepository,
                                AdvertisementRepository advertisementRepository,
                                UserService userService,
                                ImageService imageService,
                                MailService mailService,
                                PurchaseRepository purchaseRepository,
                                TaskKeyGeneratorService taskKeyGeneratorService,
                                OrganizationRepository organizationRepository,
                                AcceptanceRepository acceptanceRepository,
                                UserDetailsRepository userDetailsRepository,
                                AdMapper adMapper,
                                OrganizationService organizationService,
                                JobRepository jobRepository, PositionRepository positionRepository, PushNotificationService pushNotificationService) {
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
        this.adMapper = adMapper;
        this.organizationService = organizationService;
        this.jobRepository = jobRepository;
        this.positionRepository = positionRepository;
        this.pushNotificationService = pushNotificationService;
    }

    // get Ads in Personal account -> My advertisements
    public CustomPage<AdvertisementInterface> getPersonalAds(Long userId, Map<String, String> params) {
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
        Page<AdvertisementDto> personalAds = advertisementRepository.findPersonalAds(userId, pageable);
        return adMapper.mapToPersonalAds(personalAds);
    }

    // get Products in Personal account -> My advertisements
    private Page<AdvertisementInterface> getProducts(Long userId, Pageable pageable) {
        return productRepository.findPersonalProducts(userId, pageable);
    }

    // get Orders in Personal account -> My advertisements
    private Page<AdvertisementInterface> getOrders(Long userId, Pageable pageable) {
        return orderRepository.findPersonalOrders(userId, pageable);
    }

    // get one Ad of user in Personal account -> My advertisements
    public AdvertisementInterface getAdvertisement(Long userId, Long advertisementId) {
        return adMapper.mapToFullDto(getAdEntity(userId, advertisementId));
    }

    @Transactional
    public String interactWithAd(Long userId, Long advertisementId, byte actionId) {
        return switch (actionId) {
            case CLOSE -> closeAd(userId, advertisementId);
            case DISCLOSE -> discloseAd(userId, advertisementId);
            case DELETE -> deleteAd(userId, advertisementId);
            default -> throw new IllegalArgumentException("Unsupported action id");
        };
    }

    private String deleteAd(Long userId, Long advertisementId) {
        boolean hasAcceptedOrder = advertisementRepository.existsAcceptedOrder(advertisementId, userId);

        if (!hasAcceptedOrder) {
            int deletedRows = advertisementRepository.setDeletedByAdvertisementIdAndUserId(advertisementId, userId);
            if (deletedRows > 0) {
                return "Ad deleted";
            } else {
                return "Something went wrong";
            }
        }

        throw new ForbiddenException("You can not delete already accepted order");
    }

    private String discloseAd(Long userId, Long advertisementId) {
        int closedAdCount = advertisementRepository.setClosedFalseByAdvertisementIdAndUserId(advertisementId, userId);
        if (closedAdCount > 0) {
            return "Ad disclosed";
        } else {
            return "Something went wrong";
        }
    }

    private String closeAd(Long userId, Long advertisementId) {
        int closedAdCount = advertisementRepository.setClosedTrueByAdvertisementIdAndUserId(advertisementId, userId);
        if (closedAdCount > 0) {
            return "Ad closed";
        } else {
            return "Something went wrong";
        }
    }

    private Advertisement getAdEntity(Long userId, Long advertisementId) {
        return advertisementRepository.findByPublishedByUserIdAndAdvertisementIdAndIsDeletedFalse(userId, advertisementId)
                .orElseThrow(() -> new NotFoundException("Ad not found"));
    }

    public void updateAd(Long userId, UpdateAdRequest request, List<MultipartFile> files) {
        Advertisement advertisement = getAdEntity(userId, request.advertisementId());

        advertisement.setTitle(request.title());
        advertisement.setDescription(request.description());
        advertisement.setContactInfo(request.contactInfo());
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
            List<AdvertisementImage> advertisementImages = advertisement.getAdvertisementImages();
            if (advertisementImages == null) {
                advertisementImages = new ArrayList<>();
            }
            updateImages(advertisement, advertisementImages, files, request.imageOperations());
        }

        advertisementRepository.save(advertisement);
    }

    void updateImages(Advertisement advertisement,
                      List<AdvertisementImage> existingImages,
                      List<MultipartFile> files,
                      List<ImageOperation> imageOperationList) {
        existingImages.sort(Comparator.comparing(AdvertisementImage::getIndex));
        for (ImageOperation imageOperation : imageOperationList) {
            switch (imageOperation.action()) {
                case ADD -> {
                    if (existingImages.size() >= 5) {
                        throw new IllegalArgumentException("You can not upload more than 5 images");
                    }
                    Image newImage = imageService.processImage(files.get(imageOperation.filePosition()));
                    AdvertisementImage advertisementImage = new AdvertisementImage();
                    advertisementImage.setImage(newImage);
                    advertisementImage.setIndex(imageOperation.targetPosition());
                    advertisementImage.setAdvertisement(advertisement);
                    existingImages.add(advertisementImage);
                }
                case MOVE ->
                        Collections.swap(existingImages, imageOperation.arrayPosition(), imageOperation.targetPosition());
                case REMOVE -> existingImages.remove(imageOperation.arrayPosition());
                case REPLACE -> {
                    Image newImage = imageService.processImage(files.get(imageOperation.filePosition()));
                    AdvertisementImage advertisementImage = existingImages.get(imageOperation.arrayPosition());
                    advertisementImage.setImage(newImage);
                }
            }
        }

        IntStream.range(0, existingImages.size()).forEach(i -> existingImages.get(i).setIndex(i));
    }

    public CustomPage<PurchaseSummary> getPurchases(Long userId, Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "8")),
                Sort.by(Sort.Direction.DESC, "purchasedAt"));

        Page<PurchaseSummary> page = purchaseRepository.findAllByPurchasedUserId(userId, pageable);
        return getCustomPage(page);
    }

    public Purchase getPurchase(Long purchaseId, Long userId) {
        return purchaseRepository.findByPurchaseIdAndUserId(purchaseId, userId)
                .orElseThrow(() -> new NotFoundException("Purchase not found"));
    }

    // in Personal account -> My orders
    public CustomPage<SmallOrder> getOrders(Long userId, Map<String, String> params) {
        Sort sort = getSortProps(params);
        if (params.get("q").equals("active")) {
            Pageable pageable = PageRequest.of(
                    parseInt(params.getOrDefault("page", "0")),
                    parseInt(params.getOrDefault("size", "12")),
                    sort);
            Page<SmallOrder> page = orderRepository.findAllByPublishedByUserIdAndAcceptedByIsNotNullAndCompletedAtIsNull(userId, pageable)
                    .map(adMapper::toSmallOrder);
            return getCustomPage(page);
        }

        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "12")),
                sort);
        Page<SmallOrder> page = orderRepository.findAllByPublishedByUserIdAndCompletedAtNotNull(userId, pageable)
                .map(adMapper::toSmallOrder);
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
        return adMapper.toOrderDto(orderRepository.findByPublishedByUserIdAndAdvertisementId(userId, orderId)
                .orElseThrow(() -> new NotFoundException("Order not found")));
    }

    private static Pageable getPageable(Map<String, String> params) {
        return PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "10")),
                Sort.by(Sort.Direction.DESC, "publishedAt"));
    }

    private Advertisement getAdByIdNotDeletedNotClosed(Long advertisementId) {
        return advertisementRepository
                .findByAdvertisementIdAndIsDeletedFalseAndIsClosedFalse(advertisementId)
                .orElseThrow(() -> new NotFoundException("Advertisement not found"));
    }

    public CustomPage<Card> getMarketAds(Map<String, String> params, Authentication authentication) {
        Pageable pageable = getPageable(params);

        String type = params.get("type");

        Page<Card> page;
        page = switch (type) {
            case "orders" -> {
                Long orgId = getOrgIdFromAuthToken(authentication);
                yield orderRepository.findMarketOrders(orgId, pageable);
            }
            case "products" -> {
                Long userId = getUserIdFromAuthToken(authentication);
                yield productRepository.findMarketProducts(userId, pageable);
            }
            case "jobs" -> {
                Long orgId = getOrgIdFromAuthToken(authentication);
                yield jobRepository.findMarketJobs(orgId, pageable);
            }
            default -> Page.empty();
        };

        return getCustomPage(page);
    }

    public AdvertisementInterface getMarketAd(Long advertisementId, Authentication authentication) {
        Advertisement advertisement = getAdByIdNotDeletedNotClosed(advertisementId);

        if (advertisement instanceof OrderEntity order && order.getAcceptedAt() != null) {
            throw new NotFoundException("Advertisement not found");
        }

        return adMapper.mapToFullCard(advertisement, authentication);
    }

    @Transactional
    public String handleAdvertisement(Long advertisementId, Authentication authentication) {
        Advertisement advertisement = getAdByIdNotDeletedNotClosed(advertisementId);
        Long userId = getUserIdFromAuthToken(authentication);
        if (advertisement instanceof JobEntity job) {
            applyForJob(job, userId);
            return "Job applied";
        } else if (advertisement instanceof OrderEntity order) {
            if (order.getAcceptedAt() != null) {
                throw new MissedException("You are late!");
            }
            if (order.getPublishedBy().getUserId().equals(userId)) {
                throw new ForbiddenException("You are not allowed to accept your own order");
            }
            int authorities = getUserAuthoritiesFromToken(authentication);
            if ((authorities & Authorities.CREATE_ORDER.getBitmask()) == 0) {
                throw new ForbiddenException("You have no permission to accept orders");
            }
            Long organizationId = getOrgIdFromAuthToken(authentication);
            acceptOrder(order, organizationId);
            return "Order accepted";
        } else if (advertisement instanceof ProductEntity product) {
            purchaseProduct(product, userId);
            return "Product purchased";
        }

        throw new IllegalArgumentException("Unknown advertisement type");
    }

    private void applyForJob(JobEntity job, Long applicantId) {
        UserDetailsEntity applicant = userDetailsRepository.getReferenceById(applicantId);
        JobApplicationEntity application = new JobApplicationEntity(
                job,
                applicant,
                LocalDateTime.now(),
                ApplicationStatus.APPLIED
        );

        job.addApplication(application);

        jobRepository.save(job);
    }

    private void purchaseProduct(ProductEntity product, Long buyerId) {
        UserDetailsEntity buyer = userService.getUserDetailsEntity(buyerId);
        UserDetailsEntity seller = product.getPublishedBy();

        if (seller.getUserId().equals(buyerId)) {
            throw new ForbiddenException("You are not allowed to purchase your own product");
        }

        PurchaseEntity purchase = new PurchaseEntity(
                buyer,
                product,
                LocalDateTime.now()
        );

        product.addPurchase(purchase);

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

    public void acceptOrder(OrderEntity order, Long organizationId) {
        OrganizationEntity organization = organizationService.getOrganizationEntity(organizationId);
        if (acceptanceRepository.existsByOrganization_OrganizationIdAndOrder_AdvertisementId(
                organization.getOrganizationId(), order.getAdvertisementId())) {
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
        sendAcceptanceRequestEmail(acceptance, encryptedCode);
        String imageUrl = getFirstImageUrl(order);

        Map<String, String> data = new HashMap<>();
        data.put("sub", "Запрос о принятии заказа");
        data.put("orderId", order.getAdvertisementId().toString());
        data.put("orderImg", imageUrl);
        data.put("title", order.getTitle());
        data.put("orgId", organization.getOrganizationId().toString());
        data.put("orgName", organization.getName());
        data.put("logo", organization.getImage() == null ? "" : organization.getImage().getImageUrl());
        data.put("code", encryptedCode);

        pushNotificationService.sendToUser(order.getPublishedBy().getUserId(), data);
    }

    public String getFirstImageUrl(Advertisement advertisement) {
        if (advertisement.getAdvertisementImages() == null || advertisement.getAdvertisementImages().isEmpty()) {
            return "";
        }

        return advertisement.getAdvertisementImages().stream()
                .filter(ai -> ai.getIndex() == 0)
                .map(AdvertisementImage::getImage)
                .map(Image::getImageUrl)
                .findFirst()
                .orElse("");
    }

    private void sendAcceptanceRequestEmail(AcceptanceEntity acceptance, String encryptedCode) {
        OrderEntity order = acceptance.getOrder();
        String price = order.getPrice() == null ? null : order.getPrice().toString();
        OrganizationEntity organization = acceptance.getOrganization();
        Image image = organization.getImage();
        String imageUrl = image == null ? "" : image.getImageUrl();
        AcceptanceRequestMail request = new AcceptanceRequestMail(
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
        PositionEntity position = positionRepository.getReferenceById(request.positionId());
        List<AdvertisementImage> advertisementImages = null;
        JobEntity job = new JobEntity(
                LocalDateTime.now(),
                user,
                request.title(),
                request.description(),
                advertisementImages,
                request.contactInfo(),
                organization,
                position,
                request.jobType(),
                request.location(),
                request.salary(),
                request.applicationDeadline()
        );
        if (files != null && !files.isEmpty()) {
            advertisementImages = IntStream.range(0, files.size())
                    .mapToObj(i -> {
                        Image image = imageService.processImage(files.get(i));
                        AdvertisementImage productImage = new AdvertisementImage();
                        productImage.setImage(image);
                        productImage.setIndex(i);
                        productImage.setAdvertisement(job);
                        return productImage;
                    })
                    .toList();
            job.setAdvertisementImages(advertisementImages);
        }

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
            List<AdvertisementImage> advertisementImages = IntStream.range(0, files.size())
                    .mapToObj(i -> {
                        Image image = imageService.processImage(files.get(i));
                        AdvertisementImage productImage = new AdvertisementImage();
                        productImage.setImage(image);
                        productImage.setIndex(i);
                        productImage.setAdvertisement(order);
                        return productImage;
                    })
                    .toList();
            order.setAdvertisementImages(advertisementImages);
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
            List<AdvertisementImage> advertisementImages = IntStream.range(0, files.size())
                    .mapToObj(i -> {
                        Image image = imageService.processImage(files.get(i));
                        AdvertisementImage productImage = new AdvertisementImage();
                        productImage.setImage(image);
                        productImage.setIndex(i);
                        productImage.setAdvertisement(product);
                        return productImage;
                    })
                    .toList();
            product.setAdvertisementImages(advertisementImages);
        }

        productRepository.save(product);

        return "Product created";
    }

    @Transactional
    public void confirmOrder(String code, Long userId) {
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

        pushNotificationService.sendToOrganization(organization.getOrganizationId(), data);
    }

    @Transactional
    public void incrementViewCount(Long advertisementId) {
        advertisementRepository.incrementViewsCount(advertisementId);
    }

    public List<OrderDashboard> getDashboard(Long orgId) {
        return orderRepository.findAllDashboardOrders(orgId, COMPLETED).stream()
                .sorted(Comparator.comparing(OrderDashboard::status))
                .toList();
    }

    public MonitoringOrder getMonitoringOrder(Long userId, Long orderId) {
        OrganizationEntity organization = userService.getUserDetailsEntity(userId).getOrganization();

        OrderEntity order = getOrderEntity(orderId);

        if (!order.getAcceptedBy().getOrganizationId().equals(organization.getOrganizationId())) {
            throw new NotFoundException("Order not found");
        }

        return adMapper.mapToMonitoringOrder(order);
    }

    @Transactional
    public void updateStatus(Long userId, Long orderId, String status) {
        OrderEntity order = getOrderEntity(orderId);
        UserDetailsEntity user = userService.getUserDetailsEntity(userId);

        if (order.getAcceptedBy().getEmployees().stream().noneMatch(emp -> emp.getUserId().equals(userId))) {
            throw new NotFoundException("Order not found");
        }

        OrderStatus oldStatus = order.getStatus();
        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

        if (newStatus.equals(oldStatus)) {
            return;
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

        String avatarUrl = user.getAvatarUrl();
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

        pushNotificationService.sendToOrganization(order.getAcceptedBy().getOrganizationId(), data);
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

    public void updateJobAdvertisement(Long orgId, UpdateJobRequest request, List<MultipartFile> files) {
        JobEntity job = organizationService.getJobEntity(orgId, request.jobId());
        PositionEntity position = positionRepository.findByOrganizationOrganizationIdAndPositionId(orgId, request.positionId())
                .orElseThrow(() -> new NotFoundException("Position not found"));
        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setContactInfo(request.contactInfo());
        job.setPosition(position);
        job.setJobType(request.jobType());
        job.setLocation(request.location());
        job.setSalary(request.salary());
        job.setApplicationDeadline(request.applicationDeadline());

        if (request.imageOperations() != null && !request.imageOperations().isEmpty()) {
            List<AdvertisementImage> advertisementImages = job.getAdvertisementImages();
            if (advertisementImages == null) {
                advertisementImages = new ArrayList<>();
            }
            updateImages(job, advertisementImages, files, request.imageOperations());
        }

        jobRepository.save(job);
    }

    @Transactional
    public String interactWithJobAd(Long orgId, Long jobId, byte actionId) {
        return switch (actionId) {
            case CLOSE -> closeJobAd(orgId, jobId);
            case DISCLOSE -> discloseJobAd(orgId, jobId);
            case DELETE -> deleteJobAd(orgId, jobId);
            default -> throw new IllegalArgumentException("Unsupported action id");
        };
    }

    private String deleteJobAd(Long orgId, Long jobId) {
        int closedJobCount = jobRepository.setJobDeletedByOrganizationIdAndJobId(orgId, jobId);
        if (closedJobCount > 0) {
            return "Job deleted";
        } else {
            return "Something went wrong";
        }
    }

    private String discloseJobAd(Long orgId, Long jobId) {
        int closedJobCount = jobRepository.setJobClosedFalseByOrganizationIdAndJobId(orgId, jobId);
        if (closedJobCount > 0) {
            return "Job disclosed";
        } else {
            return "Something went wrong";
        }
    }

    private String closeJobAd(Long orgId, Long jobId) {
        int closedJobCount = jobRepository.setJobClosedTrueByOrganizationIdAndJobId(orgId, jobId);
        if (closedJobCount > 0) {
            return "Job closed";
        } else {
            return "Something went wrong";
        }
    }
}