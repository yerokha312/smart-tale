package dev.yerokha.smarttale.mapper;

import dev.yerokha.smarttale.dto.AcceptanceRequestDto;
import dev.yerokha.smarttale.dto.AdvertisementDto;
import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.AssignedEmployee;
import dev.yerokha.smarttale.dto.DashboardOrder;
import dev.yerokha.smarttale.dto.FullJobCard;
import dev.yerokha.smarttale.dto.FullOrder;
import dev.yerokha.smarttale.dto.FullOrderCard;
import dev.yerokha.smarttale.dto.FullProduct;
import dev.yerokha.smarttale.dto.FullProductCard;
import dev.yerokha.smarttale.dto.MonitoringOrder;
import dev.yerokha.smarttale.dto.Order;
import dev.yerokha.smarttale.dto.OrderDto;
import dev.yerokha.smarttale.dto.Product;
import dev.yerokha.smarttale.dto.SmallOrder;
import dev.yerokha.smarttale.dto.Task;
import dev.yerokha.smarttale.entity.AdvertisementImage;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.AcceptanceEntity;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.JobEntity;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.repository.AdvertisementRepository;
import dev.yerokha.smarttale.util.EncryptionUtil;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static dev.yerokha.smarttale.enums.PersonalAdvertisementType.ORDER;
import static dev.yerokha.smarttale.service.TokenService.getOrgIdFromAuthToken;
import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;
import static java.util.Collections.emptyList;


@Component
public class AdMapper {

    private final int DESC_LENGTH = 120;
    private final AdvertisementRepository advertisementRepository;

    public AdMapper(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    public AdvertisementInterface toFullDto(Advertisement advertisement) {
        List<String> imageUrls = advertisement.getAdvertisementImages().stream()
                .sorted(Comparator.comparing(AdvertisementImage::getIndex))
                .map(AdvertisementImage::getImage)
                .map(Image::getImageUrl)
                .toList();

        if (imageUrls.isEmpty()) {
            imageUrls = emptyList();
        }

        if (advertisement instanceof OrderEntity order) {
            return mapToFullOrder(order, imageUrls);
        }
        if (advertisement instanceof ProductEntity product) {
            return mapToFullProduct(product, imageUrls);
        }
        throw new IllegalArgumentException("Unsupported advertisement type");
    }

    private FullOrder mapToFullOrder(OrderEntity order, List<String> imageUrls) {
        boolean isAccepted = order.getAcceptedBy() != null;
        return new FullOrder(
                order.getAdvertisementId(),
                order.getPublishedAt(),
                order.getAcceptedAt(),
                isAccepted ? order.getAcceptedBy().getOrganizationId() : 0,
                order.getAcceptanceEntities() == null ? emptyList() : mapToAcceptanceDto(order.getAcceptanceEntities()),
                isAccepted ? order.getAcceptedBy().getName() : "",
                isAccepted ? getImageUrl(order.getAcceptedBy().getImage()) : "",
                order.getTitle(),
                order.getDescription(),
                order.getPrice() == null ? BigDecimal.ZERO : order.getPrice(),
                order.getSize() == null ? "" : order.getSize(),
                order.getDeadlineAt(),
                imageUrls,
                order.getViews(),
                order.isDeleted(),
                order.isClosed()
        );
    }

    private List<AcceptanceRequestDto> mapToAcceptanceDto(Set<AcceptanceEntity> entities) {
        return entities.stream().map(e -> {
                    OrganizationEntity organization = e.getOrganization();
                    return new AcceptanceRequestDto(
                            organization.getOrganizationId(),
                            organization.getName(),
                            getImageUrl(organization.getImage()),
                            EncryptionUtil.encrypt(String.valueOf(e.getAcceptanceId()))
                    );
                })
                .toList();
    }

    private FullProduct mapToFullProduct(ProductEntity product, List<String> imageUrls) {
        return new FullProduct(
                product.getAdvertisementId(),
                product.getPublishedAt(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice() == null ? BigDecimal.ZERO : product.getPrice(),
                imageUrls,
                product.getViews(),
                product.isDeleted(),
                product.isClosed()
        );
    }

    public AdvertisementInterface mapToFullCard(Advertisement advertisement, Authentication authentication) {
        String contact = advertisement.getContactInfo().toString();
        List<String> imageUrls = advertisement.getAdvertisementImages().stream()
                .sorted(Comparator.comparing(AdvertisementImage::getIndex))
                .map(AdvertisementImage::getImage)
                .map(Image::getImageUrl)
                .toList();

        if (imageUrls.isEmpty()) {
            imageUrls = emptyList();
        }
        UserDetailsEntity publishedBy = advertisement.getPublishedBy();
        if (advertisement instanceof OrderEntity order) {
            Long orgId = getOrgIdFromAuthToken(authentication);
            boolean canAccept = advertisementRepository.canAcceptOrder(orgId, order.getAdvertisementId());
            return new FullOrderCard(
                    order.getAdvertisementId(),
                    order.getPublishedAt(),
                    order.getTitle(),
                    order.getDescription(),
                    order.getPrice() == null ? BigDecimal.ZERO : order.getPrice(),
                    imageUrls,
                    order.getSize() == null ? "" : order.getSize(),
                    order.getDeadlineAt(),
                    publishedBy.getUserId(),
                    publishedBy.getName(),
                    getImageUrl(publishedBy.getImage()),
                    contact.contains("PHONE") ? publishedBy.getPhoneNumber() : "",
                    contact.contains("EMAIL") ? publishedBy.getEmail() : "",
                    order.getViews(),
                    canAccept
            );
        } else if (advertisement instanceof ProductEntity product) {
            Long userId = getUserIdFromAuthToken(authentication);
            boolean canPurchase = false;
            if (userId != null) {
                canPurchase = !userId.equals(product.getPublishedBy().getUserId());
            }
            return new FullProductCard(
                    product.getAdvertisementId(),
                    product.getPublishedAt(),
                    product.getTitle(),
                    product.getDescription(),
                    product.getPrice(),
                    imageUrls,
                    product.getPurchasedAt(),
                    publishedBy.getUserId(),
                    publishedBy.getName(),
                    getImageUrl(publishedBy.getImage()),
                    contact.contains("PHONE") ? publishedBy.getPhoneNumber() : "",
                    contact.contains("EMAIL") ? publishedBy.getEmail() : "",
                    product.getViews(),
                    canPurchase
            );
        } else if (advertisement instanceof JobEntity job) {
            OrganizationEntity organization = job.getOrganization();
            Long orgId = getOrgIdFromAuthToken(authentication);
            return new FullJobCard(
                    job.getAdvertisementId(),
                    job.getPublishedAt(),
                    job.getTitle(),
                    job.getDescription(),
                    job.getSalary() == null ? BigDecimal.ZERO : job.getSalary(),
                    imageUrls,
                    publishedBy.getUserId(),
                    publishedBy.getName(),
                    getImageUrl(publishedBy.getImage()),
                    contact.contains("PHONE") ? publishedBy.getPhoneNumber() : "",
                    contact.contains("EMAIL") ? publishedBy.getEmail() : "",
                    organization.getOrganizationId(),
                    organization.getName(),
                    getImageUrl(organization.getImage()),
                    job.getJobType(),
                    advertisementRepository.countApplicantsByJobId(job.getAdvertisementId()),
                    job.getLocation() == null ? "" : job.getLocation(),
                    job.getApplicationDeadline(),
                    job.getViews(),
                    !orgId.equals(organization.getOrganizationId())
            );
        }

        throw new IllegalArgumentException("Unsupported advertisement type");

    }

    public SmallOrder toSmallOrder(OrderEntity order) {
        return new SmallOrder(
                order.getAdvertisementId(),
                order.getTitle(),
                order.getPrice() == null ? BigDecimal.ZERO : order.getPrice(),
                order.getAcceptedAt(),
                order.getDeadlineAt(),
                order.getCompletedAt(),
                order.getStatus()
        );
    }

    public OrderDto toOrderDto(OrderEntity entity) {
        List<String> imageUrls = entity.getAdvertisementImages().stream()
                .sorted(Comparator.comparing(AdvertisementImage::getIndex))
                .map(AdvertisementImage::getImage)
                .map(Image::getImageUrl)
                .toList();

        if (imageUrls.isEmpty()) {
            imageUrls = emptyList();
        }

        OrganizationEntity acceptedOrganization = entity.getAcceptedBy();
        String logoUrl = getImageUrl(acceptedOrganization.getImage());
        return new OrderDto(
                entity.getAdvertisementId(),
                entity.getStatus(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice() == null ? BigDecimal.ZERO : entity.getPrice(),
                entity.getSize() == null ? "" : entity.getSize(),
                acceptedOrganization.getOrganizationId(),
                acceptedOrganization.getName(),
                logoUrl,
                entity.getAcceptedAt(),
                entity.getDeadlineAt(),
                entity.getCompletedAt(),
                imageUrls
        );
    }

    public DashboardOrder toDashboardOrder(OrderEntity order) {
        String comment = null;
        if (order.getComment() == null) {
            String description = order.getDescription();
            comment = description.length() >= DESC_LENGTH ? description.substring(0, DESC_LENGTH) : description;
        }

        return new DashboardOrder(
                order.getAdvertisementId(),
                order.getStatus(),
                order.getTitle(),
                order.getTaskKey() == null ? "" : order.getTaskKey(),
                comment,
                order.getDeadlineAt()
        );
    }

    public MonitoringOrder mapToMonitoringOrder(OrderEntity order) {
        UserDetailsEntity author = order.getPublishedBy();
        String contact = order.getContactInfo().toString();
        List<String> imageUrls = order.getAdvertisementImages().stream()
                .sorted(Comparator.comparing(AdvertisementImage::getIndex))
                .map(AdvertisementImage::getImage)
                .map(Image::getImageUrl)
                .toList();

        if (imageUrls.isEmpty()) {
            imageUrls = emptyList();
        }
        return new MonitoringOrder(
                order.getAdvertisementId(),
                order.getPublishedAt(),
                order.getAcceptedAt(),
                order.getDeadlineAt(),
                order.getTaskKey() == null ? "" : order.getTaskKey(),
                order.getTitle(),
                order.getDescription(),
                order.getSize() == null ? "" : order.getSize(),
                imageUrls,
                order.getStatus(),
                author.getUserId(),
                getImageUrl(author.getImage()),
                contact.contains("EMAIL") ? author.getEmail() : "",
                contact.contains("PHONE") ? author.getPhoneNumber() : "",
                order.getContractors() == null ? emptyList() : order.getContractors().stream()
                        .map(emp -> new AssignedEmployee(
                                emp.getUserId(),
                                emp.getName(),
                                getImageUrl(emp.getImage()),
                                order.getPrice() == null ? BigDecimal.ZERO : order.getPrice()
                        ))
                        .toList(),
                order.getViews()
        );
    }

    public Task toTask(OrderEntity order) {
        return new Task(
                order.getAdvertisementId(),
                order.getStatus(),
                order.getTitle(),
                order.getTaskKey() == null ? "" : order.getTaskKey(),
                order.getDescription(),
                order.getPrice() == null ? BigDecimal.ZERO : order.getPrice(),
                order.getComment() == null ? "" : order.getComment(),
                order.getCompletedAt() == null ? order.getAcceptedAt() : order.getCompletedAt(),
                order.getContractors().stream()
                        .map(emp -> new AssignedEmployee(
                                emp.getUserId(),
                                emp.getName(),
                                getImageUrl(emp.getImage()),
                                order.getPrice()
                        ))
                        .toList(),
                order.getPublishedBy().getUserId(),
                order.getPublishedBy().getName(),
                getImageUrl(order.getPublishedBy().getImage()),
                order.getPublishedBy().getPhoneNumber()
        );
    }

    private String getImageUrl(Image image) {
        if (image != null) {
            return image.getImageUrl();
        } else {
            return "";
        }
    }

    public Page<AdvertisementInterface> mapToPersonalAds(Page<AdvertisementDto> personalAds) {
        return personalAds
                .map(ad -> {
                    if (ad.type().equals(ORDER)) {
                        return new Order(
                                ad.advertisementId(),
                                ad.title(),
                                ad.description(),
                                ad.price(),
                                ad.imageUrl(),
                                ad.publishedAt(),
                                ad.acceptancesCount(),
                                ad.isClosed());
                    }

                    return new Product(
                            ad.advertisementId(),
                            ad.title(),
                            ad.description(),
                            ad.price(),
                            ad.imageUrl(),
                            ad.publishedAt(),
                            ad.isClosed()
                    );
                });
    }
}