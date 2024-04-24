package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.FullPurchase;
import dev.yerokha.smarttale.dto.ImageOperation;
import dev.yerokha.smarttale.dto.OrderDto;
import dev.yerokha.smarttale.dto.Purchase;
import dev.yerokha.smarttale.dto.SmallOrder;
import dev.yerokha.smarttale.dto.UpdateAdRequest;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.exception.NotFoundException;
import dev.yerokha.smarttale.mapper.AdMapper;
import dev.yerokha.smarttale.repository.AdvertisementRepository;
import dev.yerokha.smarttale.repository.OrderRepository;
import dev.yerokha.smarttale.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static dev.yerokha.smarttale.enums.OrderStatus.ARRIVED;
import static dev.yerokha.smarttale.enums.OrderStatus.CANCELED;
import static dev.yerokha.smarttale.enums.OrderStatus.CHECKING;
import static dev.yerokha.smarttale.enums.OrderStatus.DISPATCHED;
import static dev.yerokha.smarttale.enums.OrderStatus.IN_PROGRESS;
import static dev.yerokha.smarttale.enums.OrderStatus.NEW;
import static dev.yerokha.smarttale.mapper.AdMapper.toDto;
import static dev.yerokha.smarttale.mapper.AdMapper.toFullDto;
import static java.lang.Integer.parseInt;

@Service
public class AdvertisementService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AdvertisementRepository advertisementRepository;
    private final ImageService imageService;

    private static final byte CLOSE = 1;
    private static final byte DISCLOSE = 2;
    private static final byte DELETE = 3;
    private static final byte RESTORE = 4;

    public AdvertisementService(ProductRepository productRepository, OrderRepository orderRepository, AdvertisementRepository advertisementRepository, ImageService imageService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.advertisementRepository = advertisementRepository;
        this.imageService = imageService;
    }


    public Page<AdvertisementInterface> getAds(Long userId, Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                parseInt(params.getOrDefault("page", "0")),
                parseInt(params.getOrDefault("size", "10")),
                Sort.by(Sort.Direction.DESC, "publishedAt"));

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

    private Page<AdvertisementInterface> getAllAds(Long userId, Pageable pageable) {
        Page<Advertisement> ads = advertisementRepository.findAllByPublishedByUserId(userId, pageable);
        List<AdvertisementInterface> modifiedAds = ads.getContent().stream()
                .map(ad -> {
                    if (ad instanceof OrderEntity order) {
                        return toDto(order);
                    }
                    return toDto(ad);
                })
                .toList();
        return new PageImpl<>(modifiedAds, pageable, ads.getTotalElements());
    }

    private Page<AdvertisementInterface> getProducts(Long userIdFromAuthToken, Pageable pageable) {
        return productRepository.findAllByPublishedByUserId(userIdFromAuthToken, pageable)
                .map(AdMapper::toDto);
    }

    private Page<AdvertisementInterface> getOrders(Long userIdFromAuthToken, Pageable pageable) {
        return orderRepository.findAllByPublishedByUserId(userIdFromAuthToken, pageable)
                .map(AdMapper::toDto);
    }

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

    public Page<Purchase> getPurchases(Long userId, Map<String, String> params) {
        Pageable pageable = PageRequest.of(
                Integer.parseInt(params.getOrDefault("page", "0")),
                Integer.parseInt(params.getOrDefault("size", "8")),
                Sort.by(Sort.Direction.DESC, "purchasedAt"));
        return productRepository.findAllByPurchasedByUserId(userId, pageable)
                .map(AdMapper::mapToPurchaseDto);
    }

    public FullPurchase getPurchase(Long userId, Long productId) {
        return AdMapper.mapToFullPurchase(productRepository
                .findByPurchasedByUserIdAndAdvertisementId(userId, productId)
                .orElseThrow(() -> new NotFoundException("Purchase not found")));
    }

    public Page<SmallOrder> getOrders(Long userId, Map<String, String> params) {
        if (params.get("q").equals("active")) {
            Pageable pageable = PageRequest.of(
                    Integer.parseInt(params.getOrDefault("page", "0")),
                    Integer.parseInt(params.getOrDefault("size", "12")),
                    Sort.by(Sort.Direction.DESC, "acceptedAt"));
            return orderRepository.findAllByAcceptedByUserIdAndStatusNotIn(userId,
                            Arrays.asList(
                                    ARRIVED,
                                    CANCELED), pageable)
                    .map(AdMapper::toSmallOrder);
        }

        Pageable pageable = PageRequest.of(
                Integer.parseInt(params.getOrDefault("page", "0")),
                Integer.parseInt(params.getOrDefault("size", "12")),
                Sort.by(Sort.Direction.DESC, "completedAt"));
        return orderRepository.findAllByAcceptedByUserIdAndStatusNotIn(userId,
                        Arrays.asList(
                                NEW,
                                IN_PROGRESS,
                                CHECKING,
                                DISPATCHED), pageable)
                .map(AdMapper::toSmallOrder);

    }

    public OrderDto getOrder(Long userId, Long orderId) {
        return AdMapper.toOrderDto(orderRepository.findByAcceptedByUserIdAndAdvertisementId(userId, orderId)
                .orElseThrow(() -> new NotFoundException("Order not found")));
    }
}
