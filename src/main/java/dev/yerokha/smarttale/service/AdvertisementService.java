package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.AcceptanceRequest;
import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.Card;
import dev.yerokha.smarttale.dto.CreateAdRequest;
import dev.yerokha.smarttale.dto.ImageOperation;
import dev.yerokha.smarttale.dto.OrderDto;
import dev.yerokha.smarttale.dto.PurchaseRequest;
import dev.yerokha.smarttale.dto.SmallOrder;
import dev.yerokha.smarttale.dto.UpdateAdRequest;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.advertisement.PurchaseEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.enums.OrderStatus;
import dev.yerokha.smarttale.exception.ForbiddenException;
import dev.yerokha.smarttale.exception.MissedException;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.mapper.AdMapper;
import dev.yerokha.smarttale.repository.AdvertisementRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static dev.yerokha.smarttale.enums.OrderStatus.PENDING;
import static dev.yerokha.smarttale.mapper.AdMapper.mapToCards;
import static dev.yerokha.smarttale.mapper.AdMapper.mapToFullCard;
import static dev.yerokha.smarttale.mapper.AdMapper.toFullDto;
import static dev.yerokha.smarttale.mapper.AdMapper.toOrderDto;
import static java.lang.Integer.parseInt;

@Service
public class AdvertisementService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AdvertisementRepository advertisementRepository;
    private final ImageService imageService;
    private final UserService userService;
    private final MailService mailService;
    private final UserDetailsRepository userDetailsRepository;
    private final PurchaseRepository purchaseRepository;
    private final TaskKeyGeneratorService taskKeyGeneratorService;
    private final OrganizationRepository organizationRepository;

    private static final byte CLOSE = 1;
    private static final byte DISCLOSE = 2;
    private static final byte DELETE = 3;
    private static final byte RESTORE = 4;

    public AdvertisementService(ProductRepository productRepository,
                                OrderRepository orderRepository,
                                AdvertisementRepository advertisementRepository,
                                UserService userService,
                                ImageService imageService,
                                MailService mailService,
                                UserDetailsRepository userDetailsRepository,
                                PurchaseRepository purchaseRepository,
                                TaskKeyGeneratorService taskKeyGeneratorService,
                                OrganizationRepository organizationRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.advertisementRepository = advertisementRepository;
        this.mailService = mailService;
        this.userService = userService;
        this.imageService = imageService;
        this.userDetailsRepository = userDetailsRepository;
        this.purchaseRepository = purchaseRepository;
        this.taskKeyGeneratorService = taskKeyGeneratorService;
        this.organizationRepository = organizationRepository;
    }

    // get Ads in Personal account -> My advertisements
    public Page<AdvertisementInterface> getAds(Long userId, Map<String, String> params) {
        Pageable pageable = getPageable(params);

        String query = params.get("q");

        if (query != null) {
            return switch (query) {
                case "orders" -> getOrders(userId, pageable);
                case "products" -> getProducts(userId, pageable);
                default -> throw new IllegalStateException("Unexpected value: " + query);
            };
        }

        return getAllAds(userId, pageable);
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
        return advertisementRepository.findByPublishedByUserIdAndAdvertisementId(userId, advertisementId)
                .orElseThrow(() -> new NotFoundException("Ad not found"));
    }

    public void updateAd(Long userId, UpdateAdRequest request, List<MultipartFile> files) {
        Advertisement advertisement = getAdEntity(userId, request.advertisementId());
        advertisement.setTitle(request.title());
        advertisement.setDescription(request.description());
        advertisement.setPrice(request.price());
        if (advertisement instanceof OrderEntity order) {
            order.setDeadlineAt(request.deadlineAt());
            order.setSize(request.size());
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

    public Page<Card> getPurchases(Long userId, Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                Integer.parseInt(params.getOrDefault("page", "0")),
                Integer.parseInt(params.getOrDefault("size", "8")),
                Sort.by(Sort.Direction.DESC, "purchasedAt"));

        return purchaseRepository.findAllByPurchasedByUserId(userId, pageable)
                .map(purchase -> {
                    ProductEntity product = purchase.getProduct();
                    product.setAdvertisementId(purchase.getPurchaseId());
                    return mapToCards(product);
                });
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
    public Page<SmallOrder> getOrders(Long userId, Map<String, String> params) {
        Sort sort = getSortProps(params);
        if (params.get("q").equals("active")) {
            Pageable pageable = PageRequest.of(
                    Integer.parseInt(params.getOrDefault("page", "0")),
                    Integer.parseInt(params.getOrDefault("size", "12")),
                    sort);
            return orderRepository.findAllByPublishedByUserIdAndAcceptedByIsNotNullAndCompletedAtIsNull(userId, pageable)
                    .map(AdMapper::toSmallOrder);
        }

        Pageable pageable = PageRequest.of(
                Integer.parseInt(params.getOrDefault("page", "0")),
                Integer.parseInt(params.getOrDefault("size", "12")),
                sort);
        return orderRepository.findAllByPublishedByUserIdAndCompletedAtNotNull(userId, pageable)
                .map(AdMapper::toSmallOrder);

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
    public Page<Card> getAds(Map<String, String> params) {
        Pageable pageable = getPageable(params);

        String query = params.get("type");

        return switch (query) {
            case "orders" -> orderRepository
                    .findAllByAcceptedByIsNullAndIsClosedFalseAndIsDeletedFalse(pageable)
                    .map(AdMapper::mapToCards);
            case "products" -> productRepository
                    .findAllByIsClosedFalseAndIsDeletedFalse(pageable)
                    .map(AdMapper::mapToCards);
            default -> throw new IllegalStateException("Unexpected value: " + query);
        };

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

        product.getPurchases().add(new PurchaseEntity(
                buyer,
                product,
                LocalDateTime.now()
        ));

        productRepository.save(product);

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

    public void acceptOrder(Long advertisementId, Long userId) {
        OrderEntity order = (OrderEntity) getAdById(advertisementId);

        if (order.isClosed() || order.isDeleted()) {
            throw new NotFoundException("Advertisement not found");
        }

        if (order.getAcceptedAt() != null) {
            throw new MissedException("You are late!");
        }

        UserDetailsEntity user = userService.getUserDetailsEntity(userId);

        order.setStatus(PENDING);
        orderRepository.save(order);

        String price = order.getPrice() == null ? null : order.getPrice().toString();
        Image image = user.getOrganization().getImage();
        String imageUrl = image == null ? null : image.getImageUrl();
        AcceptanceRequest request = new AcceptanceRequest(
                order.getTitle(),
                order.getDescription(),
                price,
                user.getOrganization().getOrganizationId().toString(),
                user.getOrganization().getName(),
                imageUrl
        );

        String code = user.getOrganization().getOrganizationId() + " " + advertisementId + " " + LocalDate.now();
        String encryptedCode = "?code=" + EncryptionUtil.encrypt(code);
        mailService.sendAcceptanceRequest(order.getPublishedBy().getEmail(), request, encryptedCode);

    }

    @Transactional
    public void createAd(CreateAdRequest request, List<MultipartFile> files, Long userId) {
        if (request.type().equals("order")) {
            createOrder(request, files, userId);
            return;
        }

        createProduct(request, files, userId);
    }

    private void createOrder(CreateAdRequest request, List<MultipartFile> files, Long userId) {
        OrderEntity order = new OrderEntity();

        UserDetailsEntity user = userService.getUserDetailsEntity(userId);

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
    }

    private void createProduct(CreateAdRequest request, List<MultipartFile> files, Long userId) {
        ProductEntity product = new ProductEntity();

        UserDetailsEntity user = userService.getUserDetailsEntity(userId);

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
    }

    public void confirmOrder(String code, Long userId) {
        String[] decryptedCode = EncryptionUtil.decrypt(code).split(" ");

        LocalDate requestDate = LocalDate.parse(decryptedCode[2]);

        if (requestDate.isBefore(LocalDate.now())) {
            throw new MissedException("Link is expired");
        }

        OrderEntity order = orderRepository.findById(Long.valueOf(decryptedCode[1]))
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!order.getPublishedBy().getUserId().equals(userId)) {
            throw new ForbiddenException("It is not your order");
        }

        OrganizationEntity organization = organizationRepository.findById(Long.valueOf(decryptedCode[0]))
                .orElseThrow(() -> new NotFoundException("Organization not found"));

        order.setAcceptedBy(organization);
        order.setAcceptedAt(LocalDate.now());
        order.setStatus(OrderStatus.NEW);
        order.setTaskKey(taskKeyGeneratorService.generateTaskKey(organization));

        orderRepository.save(order);
    }

    @Transactional
    public void incrementViewCount(Long advertisementId) {
        advertisementRepository.incrementViewsCount(advertisementId);
    }
}