package dev.yerokha.smarttale.mapper;

import dev.yerokha.smarttale.dto.AcceptanceRequest;
import dev.yerokha.smarttale.dto.AdvertisementDto;
import dev.yerokha.smarttale.dto.AdvertisementInterface;
import dev.yerokha.smarttale.dto.AssignedEmployee;
import dev.yerokha.smarttale.dto.Job;
import dev.yerokha.smarttale.dto.JobApplication;
import dev.yerokha.smarttale.dto.JobCard;
import dev.yerokha.smarttale.dto.MonitoringOrder;
import dev.yerokha.smarttale.dto.OrderCard;
import dev.yerokha.smarttale.dto.OrderDto;
import dev.yerokha.smarttale.dto.OrderFull;
import dev.yerokha.smarttale.dto.OrderSummaryPersonal;
import dev.yerokha.smarttale.dto.Product;
import dev.yerokha.smarttale.dto.ProductCard;
import dev.yerokha.smarttale.dto.ProductFull;
import dev.yerokha.smarttale.dto.SmallOrder;
import dev.yerokha.smarttale.dto.Task;
import dev.yerokha.smarttale.entity.AdvertisementImage;
import dev.yerokha.smarttale.entity.Image;
import dev.yerokha.smarttale.entity.advertisement.AcceptanceEntity;
import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import dev.yerokha.smarttale.entity.advertisement.JobApplicationEntity;
import dev.yerokha.smarttale.entity.advertisement.JobEntity;
import dev.yerokha.smarttale.entity.advertisement.OrderEntity;
import dev.yerokha.smarttale.entity.advertisement.ProductEntity;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import dev.yerokha.smarttale.entity.user.PositionEntity;
import dev.yerokha.smarttale.entity.user.UserDetailsEntity;
import dev.yerokha.smarttale.repository.AdvertisementRepository;
import dev.yerokha.smarttale.util.EncryptionUtil;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static dev.yerokha.smarttale.enums.PersonalAdvertisementType.ORDER;
import static dev.yerokha.smarttale.service.TokenService.getOrgIdFromAuthToken;
import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;
import static java.util.Collections.emptyList;


@Component
public class AdMapper {

    private final AdvertisementRepository advertisementRepository;

    public AdMapper(AdvertisementRepository advertisementRepository) {
        this.advertisementRepository = advertisementRepository;
    }

    public AdvertisementInterface mapToFullDto(Advertisement advertisement) {
        List<String> imageUrls = getImageUrls(advertisement.getAdvertisementImages());

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

    private OrderFull mapToFullOrder(OrderEntity order, List<String> imageUrls) {
        boolean isAccepted = order.getAcceptedBy() != null;
        return new OrderFull(
                order.getAdvertisementId(),
                order.getPublishedAt(),
                order.getAcceptedAt(),
                isAccepted ? order.getAcceptedBy().getOrganizationId() : 0,
                order.getAcceptanceEntities() == null ? emptyList() : mapToAcceptanceDto(order.getAcceptanceEntities()),
                isAccepted ? order.getAcceptedBy().getName() : "",
                isAccepted ? order.getAcceptedBy().getLogoUrl() : "",
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

    private List<AcceptanceRequest> mapToAcceptanceDto(Set<AcceptanceEntity> entities) {
        return entities.stream().map(e -> {
                    OrganizationEntity organization = e.getOrganization();
                    return new AcceptanceRequest(
                            organization.getOrganizationId(),
                            organization.getName(),
                            organization.getLogoUrl(),
                            EncryptionUtil.encrypt(String.valueOf(e.getAcceptanceId()))
                    );
                })
                .toList();
    }

    private ProductFull mapToFullProduct(ProductEntity product, List<String> imageUrls) {
        return new ProductFull(
                product.getAdvertisementId(),
                product.getPublishedAt(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice() == null ? BigDecimal.ZERO : product.getPrice(),
                product.getQuantity(),
                imageUrls,
                product.getViews(),
                product.isDeleted(),
                product.isClosed()
        );
    }

    public AdvertisementInterface mapToFullCard(Advertisement advertisement, Authentication authentication) {
        String contact = advertisement.getContactInfo().toString();
        List<String> imageUrls = getImageUrls(advertisement.getAdvertisementImages());

        if (imageUrls.isEmpty()) {
            imageUrls = emptyList();
        }
        UserDetailsEntity publishedBy = advertisement.getPublishedBy();
        if (advertisement instanceof OrderEntity order) {
            return getFullOrderCard(authentication, order, imageUrls, publishedBy, contact);
        } else if (advertisement instanceof ProductEntity product) {
            return getFullProductCard(authentication, product, imageUrls, publishedBy, contact);
        } else if (advertisement instanceof JobEntity job) {
            return getFullJobCard(authentication, job, imageUrls, publishedBy, contact);
        }

        throw new IllegalArgumentException("Unsupported advertisement type");

    }

    private JobCard getFullJobCard(Authentication authentication, JobEntity job, List<String> imageUrls, UserDetailsEntity publishedBy, String contact) {
        OrganizationEntity organization = job.getOrganization();
        Long orgId = getOrgIdFromAuthToken(authentication);
        return new JobCard(
                job.getAdvertisementId(),
                job.getPublishedAt(),
                job.getTitle(),
                job.getDescription(),
                job.getSalary() == null ? BigDecimal.ZERO : job.getSalary(),
                imageUrls,
                publishedBy.getUserId(),
                publishedBy.getName(),
                publishedBy.getAvatarUrl(),
                contact.contains("PHONE") ? publishedBy.getPhoneNumber() : "",
                contact.contains("EMAIL") ? publishedBy.getEmail() : "",
                organization.getOrganizationId(),
                organization.getName(),
                organization.getLogoUrl(),
                job.getJobType(),
                advertisementRepository.countApplicantsByJobId(job.getAdvertisementId()),
                job.getLocation() == null ? "" : job.getLocation(),
                job.getApplicationDeadline(),
                job.getViews(),
                !orgId.equals(organization.getOrganizationId())
        );
    }

    private ProductCard getFullProductCard(Authentication authentication, ProductEntity product, List<String> imageUrls, UserDetailsEntity publishedBy, String contact) {
        Long userId = getUserIdFromAuthToken(authentication);
        boolean canPurchase = false;
        if (userId != null) {
            canPurchase = !userId.equals(product.getPublishedBy().getUserId()) || (product.getQuantity() < 1);
        }
        return new ProductCard(
                product.getAdvertisementId(),
                product.getPublishedAt(),
                product.getTitle(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                imageUrls,
                product.getPurchasedAt(),
                publishedBy.getUserId(),
                publishedBy.getName(),
                publishedBy.getAvatarUrl(),
                contact.contains("PHONE") ? publishedBy.getPhoneNumber() : "",
                contact.contains("EMAIL") ? publishedBy.getEmail() : "",
                product.getViews(),
                canPurchase
        );
    }

    private OrderCard getFullOrderCard(Authentication authentication, OrderEntity order, List<String> imageUrls, UserDetailsEntity publishedBy, String contact) {
        Long orgId = getOrgIdFromAuthToken(authentication);
        boolean canAccept = advertisementRepository.canAcceptOrder(orgId, order.getAdvertisementId());
        return new OrderCard(
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
                publishedBy.getAvatarUrl(),
                contact.contains("PHONE") ? publishedBy.getPhoneNumber() : "",
                contact.contains("EMAIL") ? publishedBy.getEmail() : "",
                order.getViews(),
                canAccept
        );
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
        List<String> imageUrls = getImageUrls(entity.getAdvertisementImages());

        if (imageUrls.isEmpty()) {
            imageUrls = emptyList();
        }

        OrganizationEntity acceptedOrganization = entity.getAcceptedBy();
        String logoUrl = acceptedOrganization.getLogoUrl();
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

    public MonitoringOrder mapToMonitoringOrder(OrderEntity order) {
        UserDetailsEntity author = order.getPublishedBy();
        String contact = order.getContactInfo().toString();
        List<String> imageUrls = getImageUrls(order.getAdvertisementImages());

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
                author.getAvatarUrl(),
                contact.contains("EMAIL") ? author.getEmail() : "",
                contact.contains("PHONE") ? author.getPhoneNumber() : "",
                order.getContractors() == null ? emptyList() : order.getContractors().stream()
                        .map(emp -> new AssignedEmployee(
                                emp.getUserId(),
                                emp.getName(),
                                emp.getAvatarUrl(),
                                order.getPrice() == null ? BigDecimal.ZERO : order.getPrice()
                        ))
                        .toList(),
                order.getViews()
        );
    }

    public Task toTask(OrderEntity order) {
        UserDetailsEntity publishedBy = order.getPublishedBy();
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
                                emp.getAvatarUrl(),
                                order.getPrice()
                        ))
                        .toList(),
                publishedBy.getUserId(),
                publishedBy.getName(),
                publishedBy.getAvatarUrl(),
                publishedBy.getPhoneNumber()
        );
    }

    public Page<AdvertisementInterface> mapToPersonalAds(Page<AdvertisementDto> personalAds) {
        return personalAds
                .map(ad -> {
                    if (ad.type().equals(ORDER)) {
                        return new OrderSummaryPersonal(
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
                            ad.quantity(),
                            ad.imageUrl(),
                            ad.publishedAt(),
                            ad.isClosed()
                    );
                });
    }

    private static List<String> getImageUrls(List<AdvertisementImage> images) {
        return images.stream()
                .sorted(Comparator.comparing(AdvertisementImage::getIndex))
                .map(AdvertisementImage::getImage)
                .map(Image::getImageUrl)
                .toList();
    }

    public Job mapToJob(JobEntity job, int userHierarchy, int userAuthorities) {
        UserDetailsEntity userDetails = job.getPublishedBy();
        PositionEntity position = job.getPosition();
        int positionHierarchy = position.getHierarchy();
        int positionAuthorities = position.getAuthorities();
        boolean canModify = (positionHierarchy > userHierarchy) && ((positionAuthorities & userAuthorities) > 0);
        return new Job(
                job.getAdvertisementId(),
                job.getPublishedAt(),
                userDetails.getUserId(),
                userDetails.getName(),
                userDetails.getAvatarUrl(),
                job.getTitle(),
                position.getPositionId(),
                position.getTitle(),
                job.getJobType(),
                job.getLocation(),
                job.getSalary() == null ? BigDecimal.ZERO : job.getSalary(),
                job.getDescription(),
                getImageUrls(job.getAdvertisementImages()),
                getJobApplications(job.getApplications()),
                job.getApplicationDeadline(),
                job.getViews(),
                job.isDeleted(),
                job.isClosed(),
                canModify
        );
    }

    private List<JobApplication> getJobApplications(List<JobApplicationEntity> applications) {
        if (applications == null || applications.isEmpty()) {
            return Collections.emptyList();
        }
        return applications.stream()
                .map(a -> {
                    UserDetailsEntity applicant = a.getApplicant();
                    return new JobApplication(
                            a.getApplicationId(),
                            a.getApplicationDate(),
                            applicant.getUserId(),
                            applicant.getName(),
                            applicant.getAvatarUrl(),
                            applicant.getEmail(),
                            applicant.getPhoneNumber()
                    );
                })
                .toList();
    }
}