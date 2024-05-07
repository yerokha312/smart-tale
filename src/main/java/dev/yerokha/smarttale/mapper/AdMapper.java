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
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;

import java.util.List;

public class AdMapper {

    public static AdvertisementInterface toDto(Advertisement advertisement) {
        String imageUrl = null;
        List<Image> images = advertisement.getImages();
        if (images != null && !images.isEmpty()) {
            imageUrl = images.get(0).getImageUrl();
        }
        String description = advertisement.getDescription();
        String truncatedDescription = description.length() >= 40 ? description.substring(0, 40) : description;
        if (advertisement instanceof OrderEntity) {
            return new Order(
                    advertisement.getAdvertisementId(),
                    advertisement.getTitle(),
                    truncatedDescription,
                    advertisement.getPrice(),
                    imageUrl,
                    advertisement.getPublishedAt()
            );
        }

        return new Product(
                advertisement.getAdvertisementId(),
                advertisement.getTitle(),
                truncatedDescription,
                advertisement.getPrice(),
                imageUrl,
                advertisement.getPublishedAt()
        );
    }

    public static AdvertisementInterface toFullDto(Advertisement advertisement) {
        List<String> imageUrls = getImageUrls(advertisement.getImages());


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
        String imageUrl = null;
        List<Image> images = advertisement.getImages();
        if (images != null && !images.isEmpty()) {
            imageUrl = images.get(0).getImageUrl();
        }
        UserDetailsEntity publishedBy = advertisement.getPublishedBy();
        Image avatar = publishedBy.getImage();
        String description = advertisement.getDescription();
        String truncatedDescription = description.length() >= 40 ? description.substring(0, 40) : description;
        String publisherAvatarUrl = avatar == null ? null : avatar.getImageUrl();

        return new Card(
                advertisement.getAdvertisementId(),
                advertisement.getPublishedAt(),
                advertisement.getTitle(),
                truncatedDescription,
                advertisement.getPrice(),
                imageUrl,
                publishedBy.getUserId(),
                publishedBy.getName(),
                publisherAvatarUrl
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
                    isAccepted && acceptedBy.getImage() != null ? acceptedBy.getImage().getImageUrl() : null,
                    result.user().getUserId(),
                    result.publisherName(),
                    result.avatar() != null ? result.avatar().getImageUrl() : null,
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
                    result.avatar() == null ? null : result.avatar().getImageUrl(),
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
        String logoUrl = acceptedOrganization.getImage() == null ? null : acceptedOrganization.getImage().getImageUrl();
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
        List<String> imageUrls = getImageUrls(entity.getImages());

        UserDetailsEntity user = entity.getPublishedBy();
        Image avatar = user.getImage();
        String publisherName = user.getLastName() + " " + user.getFirstName() + " " + user.getMiddleName();
        return new Result(imageUrls, avatar, user, publisherName);
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
                getImageUrls(order.getImages()),
                order.getStatus(),
                author.getUserId(),
                author.getImage() == null ? null : author.getImage().getImageUrl(),
                contact.contains("EMAIL") ? author.getEmail() : null,
                contact.contains("PHONE") ? author.getPhoneNumber() : null,
                order.getContractors() == null ? null : order.getContractors().stream()
                        .map(emp -> new AssignedEmployee(
                                emp.getUserId(),
                                emp.getName(),
                                emp.getImage() == null ? null : emp.getImage().getImageUrl()
                        ))
                        .toList(),
                order.getViews()
        );
    }

    private record Result(List<String> imageUrls, Image avatar, UserDetailsEntity user, String publisherName) {
    }

    private static List<String> getImageUrls(List<Image> images) {
        List<String> imageUrls = null;
        if (images != null && !images.isEmpty()) {
            imageUrls = images.stream()
                    .map(Image::getImageUrl)
                    .toList();
        }
        return imageUrls;
    }

    public static OrderSummary toCurrentOrder(OrderEntity order) {
        List<Image> images = order.getImages();
        String description = order.getDescription();
        String truncatedDescription = description.length() >= 40 ? description.substring(0, 40) : description;
        return new OrderSummary(
                order.getAdvertisementId(),
                order.getTitle(),
                truncatedDescription,
                order.getPrice(),
                images == null ? null : images.isEmpty() ? null : images.get(0).getImageUrl(),
                order.getStatus(),
                order.getAcceptedAt(),
                order.getDeadlineAt(),
                order.getCompletedAt());
    }
}