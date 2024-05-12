package dev.yerokha.smarttale.mapper;

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
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;

import java.util.List;

public class AdMapper {

    public static AdvertisementInterface toDto(Advertisement advertisement) {
        String description = advertisement.getDescription();
        String truncatedDescription = description.length() >= 40 ? description.substring(0, 40) : description;
        List<Image> images = advertisement.getImages();
        if (advertisement instanceof OrderEntity) {
            return new Order(
                    advertisement.getAdvertisementId(),
                    advertisement.getTitle(),
                    truncatedDescription,
                    advertisement.getPrice(),
                    images == null || images.isEmpty() ? null : getImageUrl(images.get(0)),
                    advertisement.getPublishedAt()
            );
        }

        return new Product(
                advertisement.getAdvertisementId(),
                advertisement.getTitle(),
                truncatedDescription,
                advertisement.getPrice(),
                images == null || images.isEmpty() ? null : getImageUrl(images.get(0)),
                advertisement.getPublishedAt()
        );
    }

    public static AdvertisementInterface toFullDto(Advertisement advertisement) {
        List<String> imageUrls = advertisement.getImages().stream()
                .map(Image::getImageUrl)
                .toList();


        if (advertisement instanceof OrderEntity order) {
            return mapToFullOrder(order, imageUrls);
        }
        if (advertisement instanceof ProductEntity product) {
            return mapToFullProduct(product, imageUrls);
        }
        throw new IllegalArgumentException("Unsupported advertisement type");
    }

    private static FullOrder mapToFullOrder(OrderEntity order, List<String> imageUrls) {
        return new FullOrder(
                order.getAdvertisementId(),
                order.getPublishedAt(),
                order.getAcceptedAt(),
                order.getAcceptedBy().getOrganizationId(),
                order.getAcceptedBy().getName(),
                order.getAcceptedBy().getImage() == null ? null : order.getAcceptedBy().getImage().getImageUrl(),
                order.getTitle(),
                order.getDescription(),
                order.getPrice(),
                order.getSize(),
                order.getDeadlineAt(),
                imageUrls,
                order.getViews(),
                order.isDeleted(),
                order.isClosed()
        );
    }

    private static FullProduct mapToFullProduct(ProductEntity product, List<String> imageUrls) {
        return new FullProduct(
                product.getAdvertisementId(),
                product.getPublishedAt(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
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
        String truncatedDescription = description.length() >= 40 ? description.substring(0, 40) : description;
        List<Image> images = advertisement.getImages();
        return new Card(
                advertisement.getAdvertisementId(),
                advertisement.getPublishedAt(),
                advertisement.getTitle(),
                truncatedDescription,
                advertisement.getPrice(),
                images == null || images.isEmpty() ? null : getImageUrl(advertisement.getImages().get(0)),
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
                    order.getPrice(),
                    result.imageUrls(),
                    order.getSize(),
                    order.getPublishedAt(),
                    order.getDeadlineAt(),
                    isAccepted ? acceptedBy.getOrganizationId() : null,
                    isAccepted ? acceptedBy.getName() : null,
                    isAccepted ? getImageUrl(acceptedBy.getImage()) : null,
                    result.user().getUserId(),
                    result.publisherName(),
                    result.avatarUrl(),
                    contact.contains("PHONE") ? result.user().getPhoneNumber() : null,
                    contact.contains("EMAIL") ? result.user().getEmail() : null,
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
                    contact.contains("PHONE") ? result.user().getPhoneNumber() : null,
                    contact.contains("EMAIL") ? result.user().getEmail() : null,
                    product.getViews()
            );
        }

        throw new IllegalArgumentException("Unsupported advertisement type");

    }

    public static SmallOrder toSmallOrder(OrderEntity order) {
        return new SmallOrder(
                order.getAdvertisementId(),
                order.getTitle(),
                order.getPrice(),
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
                entity.getPrice(),
                entity.getSize(),
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
            comment = description.length() >= 40 ? description.substring(0, 40) : description;
        }

        return new DashboardOrder(
                order.getAdvertisementId(),
                order.getStatus(),
                order.getTitle(),
                order.getTaskKey(),
                comment,
                order.getDeadlineAt()
        );
    }

    public static MonitoringOrder mapToMonitoringOrder(OrderEntity order) {
        UserDetailsEntity author = order.getPublishedBy();
        String contact = order.getContactInfo().toString();
        return new MonitoringOrder(
                order.getAdvertisementId(),
                order.getPublishedAt(),
                order.getAcceptedAt(),
                order.getDeadlineAt(),
                order.getTaskKey(),
                order.getTitle(),
                order.getDescription(),
                order.getSize(),
                order.getImages().stream().map(AdMapper::getImageUrl).toList(),
                order.getStatus(),
                author.getUserId(),
                getImageUrl(author.getImage()),
                contact.contains("EMAIL") ? author.getEmail() : null,
                contact.contains("PHONE") ? author.getPhoneNumber() : null,
                order.getContractors() == null ? null : order.getContractors().stream()
                        .map(emp -> new AssignedEmployee(
                                emp.getUserId(),
                                emp.getName(),
                                getImageUrl(emp.getImage()),
                                order.getPrice()
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
                order.getTaskKey(),
                order.getDescription(),
                order.getPrice(),
                order.getComment(),
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
            return null;
        }
    }

    public static OrderSummary toCurrentOrder(OrderEntity order) {
        List<Image> images = order.getImages();
        String description = order.getDescription();
        String truncatedDescription = description.length() >= 40 ? description.substring(0, 40) : description;
        return new OrderSummary(
                order.getAdvertisementId(),
                order.getTaskKey(),
                order.getTitle(),
                truncatedDescription,
                order.getPrice(),
                images == null || images.isEmpty() ? null : getImageUrl(images.get(0)),
                order.getStatus(),
                order.getAcceptedAt(),
                order.getDeadlineAt(),
                order.getCompletedAt());
    }
}