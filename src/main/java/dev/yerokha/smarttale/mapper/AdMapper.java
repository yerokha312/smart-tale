package dev.yerokha.smarttale.mapper;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.CurrentOrder;
import dev.yerokha.smarttale.dto.FullOrder;
import dev.yerokha.smarttale.dto.FullProduct;
import dev.yerokha.smarttale.dto.FullPurchase;
import dev.yerokha.smarttale.dto.Order;
import dev.yerokha.smarttale.dto.OrderDto;
import dev.yerokha.smarttale.dto.Product;
import dev.yerokha.smarttale.dto.Purchase;
import dev.yerokha.smarttale.dto.SmallOrder;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;

import java.util.ArrayList;
import java.util.List;

public class AdMapper {

    public static AdvertisementInterface toDto(Advertisement advertisement) {
        String imageUrl = null;
        List<Image> images = advertisement.getImages();
        if (images != null && !images.isEmpty()) {
            imageUrl = images.get(0).getImageUrl();
        }
        if (advertisement instanceof OrderEntity) {
            return new Order(
                    advertisement.getAdvertisementId(),
                    advertisement.getTitle(),
                    advertisement.getDescription(),
                    advertisement.getPrice(),
                    imageUrl,
                    advertisement.getPublishedAt()
            );
        }

        return new Product(
                advertisement.getAdvertisementId(),
                advertisement.getTitle(),
                advertisement.getDescription(),
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
                getUserId(order.getPublishedBy()),
                order.getAcceptedAt(),
                getUserId(order.getAcceptedBy()),
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
                getUserId(product.getPublishedBy()),
                product.getPurchasedAt(),
                getUserId(product.getPurchasedBy()),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                imageUrls,
                product.getViews(),
                product.isDeleted(),
                product.isClosed()
        );
    }

    public static Purchase mapToPurchaseDto(ProductEntity entity) {
        String imageUrl = null;
        List<Image> images = entity.getImages();
        if (images != null && !images.isEmpty()) {
            imageUrl = images.get(0).getImageUrl();
        }
        UserDetailsEntity publishedBy = entity.getPublishedBy();
        Image avatar = publishedBy.getImage();
        return new Purchase(
                entity.getAdvertisementId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                imageUrl,
                entity.getPurchasedAt(),
                publishedBy.getUserId(),
                avatar == null ? null : avatar.getImageUrl()
        );
    }

    public static FullPurchase mapToFullPurchase(ProductEntity entity) {
        Result result = getResult(entity);
        return new FullPurchase(
                entity.getAdvertisementId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                result.imageUrls(),
                entity.getPurchasedAt(),
                result.user().getUserId(),
                result.publisherName(),
                result.avatar() == null ? null : result.avatar().getImageUrl(),
                result.user().getPhoneNumber(),
                result.user().getEmail()
        );
    }

    private static Long getUserId(UserDetailsEntity user) {
        return user != null ? user.getUserId() : null;
    }

    public static SmallOrder toSmallOrder(OrderEntity order) {
        return new SmallOrder(
                order.getAdvertisementId(),
                order.getTitle(),
                order.getPrice(),
                order.getAcceptedAt()
        );
    }

    public static OrderDto toOrderDto(OrderEntity entity) {
        Result result = getResult(entity);
        return new OrderDto(
                entity.getAdvertisementId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getSize(),
                entity.getDeadlineAt(),
                result.imageUrls(),
                result.user().getUserId(),
                result.avatar() == null ? null : result.avatar().getImageUrl(),
                result.publisherName(),
                result.user().getPhoneNumber(),
                result.user().getEmail(),
                entity.getCompletedAt() == null ? entity.getAcceptedAt() : entity.getCompletedAt()
        );
    }

    private static Result getResult(Advertisement entity) {
        List<String> imageUrls = getImageUrls(entity.getImages());

        UserDetailsEntity user = entity.getPublishedBy();
        Image avatar = user.getImage();
        String publisherName = user.getLastName() + " " + user.getFirstName() + " " + user.getMiddleName();
        return new Result(imageUrls, avatar, user, publisherName);
    }

    private record Result(List<String> imageUrls, Image avatar, UserDetailsEntity user, String publisherName) {
    }

    private static List<String> getImageUrls(List<Image> order) {
        List<String> imageUrls = new ArrayList<>();
        if (order != null && !order.isEmpty()) {
            imageUrls = order.stream()
                    .map(Image::getImageUrl)
                    .toList();
        }
        return imageUrls;
    }

    public static CurrentOrder toCurrentOrder(OrderEntity order) {
        List<Image> images = order.getImages();
        return new CurrentOrder(
                order.getAdvertisementId(),
                order.getTitle(),
                order.getDescription(),
                order.getPrice(),
                images == null ? null : images.isEmpty() ? null : images.get(0).getImageUrl(),
                order.getStatus());
    }
}