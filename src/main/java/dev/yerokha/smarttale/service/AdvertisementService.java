package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.yerokha.smarttale.mapper.AdMapper.toDto;
import static dev.yerokha.smarttale.mapper.AdMapper.toFullDto;
import static java.lang.Integer.parseInt;

@Service
public class AdvertisementService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final AdvertisementRepository advertisementRepository;

    private static final byte CLOSE = 1;
    private static final byte DISCLOSE = 2;
    private static final byte DELETE = 3;
    private static final byte RESTORE = 4;

    public AdvertisementService(ProductRepository productRepository, OrderRepository orderRepository, AdvertisementRepository advertisementRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.advertisementRepository = advertisementRepository;
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
                    } else {
                        return toDto(ad);
                    }
                })
                .collect(Collectors.toList());
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
}
