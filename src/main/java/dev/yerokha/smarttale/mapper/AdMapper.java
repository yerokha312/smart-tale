package dev.yerokha.smarttale.mapper;

import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.FullOrder;
import dev.yerokha.smarttale.dto.FullProduct;
import dev.yerokha.smarttale.dto.Order;
import dev.yerokha.smarttale.dto.Product;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;

import java.util.List;

public class AdMapper {

    public static AdvertisementInterface toDto(Advertisement advertisement) {
        String imageUrl = null;
        if (advertisement.getImages() != null && !advertisement.getImages().isEmpty()) {
            imageUrl = advertisement.getImages().get(0).getImageUrl();
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
                getUserId(order.getPublishedBy()),
                order.getAcceptedAt(),
                getUserId(order.getAcceptedBy()),
                order.getTitle(),
                order.getDescription(),
                order.getPrice(),
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

    private static Long getUserId(UserDetailsEntity user) {
        return user != null ? user.getUserId() : null;
    }

}