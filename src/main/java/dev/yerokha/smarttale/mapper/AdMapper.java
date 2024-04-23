package dev.yerokha.smarttale.mapper;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.FullOrder;
import dev.yerokha.smarttale.dto.FullProduct;
import dev.yerokha.smarttale.dto.FullPurchase;
import dev.yerokha.smarttale.dto.Order;
import dev.yerokha.smarttale.dto.Product;
import dev.yerokha.smarttale.dto.Purchase;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.entity.user.UserEntity;

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
        List<Image> images = advertisement.getImages();
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            imageUrls = images.stream()
                    .map(Image::getImageUrl)
                    .toList();
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
        List<Image> images = entity.getImages();
        List<String> imageUrls = new ArrayList<>();
        if (images != null && !images.isEmpty()) {
            imageUrls = images.stream()
                    .map(Image::getImageUrl)
                    .toList();
        }

        UserDetailsEntity publishedBy = entity.getPublishedBy();
        Image avatar = publishedBy.getImage();
        UserEntity user = publishedBy.getUser();
        String publisherName = user.getLastName() + " " + user.getFirstName() + " " + user.getFatherName();
        return new FullPurchase(
                entity.getAdvertisementId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getPrice(),
                imageUrls,
                entity.getPurchasedAt(),
                publishedBy.getUserId(),
                publisherName,
                avatar == null ? null : avatar.getImageUrl(),
                user.getPhoneNumber(),
                user.getEmail()
        );
    }

    private static Long getUserId(UserDetailsEntity user) {
        return user != null ? user.getUserId() : null;
    }

}