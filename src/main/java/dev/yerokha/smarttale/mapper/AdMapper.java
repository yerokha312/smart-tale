package dev.yerokha.smarttale.mapper;

import dev.yerokha.smarttale.dto.AcceptanceRequestDto;
import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.AssignedEmployee;
import dev.yerokha.smarttale.dto.Card;
import dev.yerokha.smarttale.dto.DashboardOrder;
import dev.yerokha.smarttale.dto.FullOrder;
import dev.yerokha.smarttale.dto.FullOrderCard;
import dev.yerokha.smarttale.dto.FullProduct;
import dev.yerokha.smarttale.dto.FullProductCard;
import dev.yerokha.smarttale.dto.MonitoringOrder;
import dev.yerokha.smarttale.dto.Order;
import dev.yerokha.smarttale.dto.OrderDto;
import dev.yerokha.smarttale.dto.OrderSummary;
import dev.yerokha.smarttale.dto.Product;
import dev.yerokha.smarttale.dto.SmallOrder;
import dev.yerokha.smarttale.dto.Task;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.AcceptanceEntity;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.util.EncryptionUtil;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;

public class AdMapper {

    private static final int DESC_LENGTH = 120;

    public static AdvertisementInterface toDto(Advertisement advertisement) {
        String description = advertisement.getDescription();
        String truncatedDescription = description.length() >= DESC_LENGTH ? description.substring(0, DESC_LENGTH) : description;
        List<Image> images = advertisement.getImages();
        if (advertisement instanceof OrderEntity order) {
            int acceptancesCount = Hibernate.size(order.getAcceptanceEntities());
            return new Order(
                    order.getAdvertisementId(),
                    order.getTitle(),
                    truncatedDescription,
                    order.getPrice() == null ? BigDecimal.ZERO : order.getPrice(),
                    images == null || images.isEmpty() ? "" : getImageUrl(images.get(0)),
                    order.getPublishedAt(),
                    acceptancesCount,
                    order.isClosed()
            );
        }

        return new Product(
                advertisement.getAdvertisementId(),
                advertisement.getTitle(),
                truncatedDescription,
                advertisement.getPrice() == null ? BigDecimal.ZERO : advertisement.getPrice(),
                images == null || images.isEmpty() ? "" : getImageUrl(images.get(0)),
                advertisement.getPublishedAt(),
                advertisement.isClosed()
        );
    }

    public static AdvertisementInterface toFullDto(Advertisement advertisement) {
        List<String> imageUrls = advertisement.getImages().stream()
                .map(Image::getImageUrl)
                .toList();

        if (imageUrls.isEmpty()) {
            imageUrls = Collections.emptyList();
        }

        if (advertisement instanceof OrderEntity order) {
            return mapToFullOrder(order, imageUrls);
        }
        if (advertisement instanceof ProductEntity product) {
            return mapToFullProduct(product, imageUrls);
        }
        throw new IllegalArgumentException("Unsupported advertisement type");
    }

    private static FullOrder mapToFullOrder(OrderEntity order, List<String> imageUrls) {
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

    private static List<AcceptanceRequestDto> mapToAcceptanceDto(Set<AcceptanceEntity> entities) {
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

    private static FullProduct mapToFullProduct(ProductEntity product, List<String> imageUrls) {
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

    public static Card mapToCards(Advertisement advertisement) {
        UserDetailsEntity publishedBy = advertisement.getPublishedBy();
        String avatarUrl = getImageUrl(publishedBy.getImage());
        String description = advertisement.getDescription();
        String truncatedDescription = description.length() >= DESC_LENGTH ? description.substring(0, DESC_LENGTH) : description;
        List<Image> images = advertisement.getImages();
        return new Card(
                advertisement.getAdvertisementId(),
                advertisement.getPublishedAt(),
                advertisement.getTitle(),
                truncatedDescription,
                advertisement.getPrice() == null ? BigDecimal.ZERO : advertisement.getPrice(),
                images == null || images.isEmpty() ? "" : getImageUrl(advertisement.getImages().get(0)),
                publishedBy.getUserId(),
                publishedBy.getName(),
                avatarUrl
        );
    }

    public static AdvertisementInterface mapToFullCard(Advertisement entity) {
        Result result = getResult(entity);
        String contact = entity.getContactInfo().toString();
        if (entity instanceof OrderEntity order) {
            OrganizationEntity acceptedBy = order.getAcceptedBy();
            boolean isAccepted = acceptedBy != null;
            return new FullOrderCard(
                    order.getAdvertisementId(),
                    order.getTitle(),
                    order.getDescription(),
                    order.getPrice() == null ? BigDecimal.ZERO : order.getPrice(),
                    result.imageUrls(),
                    order.getSize() == null ? "" : order.getSize(),
                    order.getPublishedAt(),
                    order.getDeadlineAt(),
                    isAccepted ? acceptedBy.getOrganizationId() : 0,
                    isAccepted ? acceptedBy.getName() : "",
                    isAccepted ? getImageUrl(acceptedBy.getImage()) : "",
                    result.user().getUserId(),
                    result.publisherName(),
                    result.avatarUrl(),
                    contact.contains("PHONE") ? result.user().getPhoneNumber() : "",
                    contact.contains("EMAIL") ? result.user().getEmail() : "",
                    order.getViews()
            );
        }

        if (entity instanceof ProductEntity product) {
            return new FullProductCard(
                    product.getAdvertisementId(),
                    product.getTitle(),
                    product.getDescription(),
                    product.getPrice(),
                    result.imageUrls(),
                    product.getPublishedAt(),
                    product.getPurchasedAt(),
                    result.user().getUserId(),
                    result.publisherName(),
                    result.avatarUrl(),
                    contact.contains("PHONE") ? result.user().getPhoneNumber() : "",
                    contact.contains("EMAIL") ? result.user().getEmail() : "",
                    product.getViews()
            );
        }

        throw new IllegalArgumentException("Unsupported advertisement type");

    }

    public static SmallOrder toSmallOrder(OrderEntity order) {
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

    public static OrderDto toOrderDto(OrderEntity entity) {
        Result result = getResult(entity);
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
                result.imageUrls()
        );
    }

    private static Result getResult(Advertisement entity) {
        List<String> imageUrls = entity.getImages().stream()
                .map(AdMapper::getImageUrl)
                .toList();

        if (imageUrls.isEmpty()) {
            imageUrls = emptyList();
        }
        UserDetailsEntity user = entity.getPublishedBy();
        String avatarUrl = getImageUrl(user.getImage());
        String publisherName = user.getLastName() + " " + user.getFirstName() + " " + user.getMiddleName();
        return new Result(imageUrls, avatarUrl, user, publisherName);
    }

    private record Result(List<String> imageUrls, String avatarUrl, UserDetailsEntity user, String publisherName) {
    }

    public static DashboardOrder toDashboardOrder(OrderEntity order) {
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

    public static MonitoringOrder mapToMonitoringOrder(OrderEntity order) {
        UserDetailsEntity author = order.getPublishedBy();
        String contact = order.getContactInfo().toString();
        List<String> list = order.getImages() != null ?
                order.getImages().stream().map(AdMapper::getImageUrl).toList() :
                Collections.emptyList();
        return new MonitoringOrder(
                order.getAdvertisementId(),
                order.getPublishedAt(),
                order.getAcceptedAt(),
                order.getDeadlineAt(),
                order.getTaskKey() == null ? "" : order.getTaskKey(),
                order.getTitle(),
                order.getDescription(),
                order.getSize() == null ? "" : order.getSize(),
                list,
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

    public static Task toTask(OrderEntity order) {
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

    private static String getImageUrl(Image image) {
        if (image != null) {
            return image.getImageUrl();
        } else {
            return "";
        }
    }

    public static OrderSummary toCurrentOrder(OrderEntity order) {
        List<Image> images = order.getImages();
        String description = order.getDescription();
        String truncatedDescription = description.length() >= DESC_LENGTH ? description.substring(0, DESC_LENGTH) : description;
        return new OrderSummary(
                order.getAdvertisementId(),
                order.getTaskKey() == null ? "" : order.getTaskKey(),
                order.getTitle(),
                truncatedDescription,
                order.getPrice() == null ? BigDecimal.ZERO : order.getPrice(),
                images == null || images.isEmpty() ? "" : getImageUrl(images.get(0)),
                order.getStatus(),
                order.getAcceptedAt(),
                order.getDeadlineAt(),
                order.getCompletedAt());
    }
}