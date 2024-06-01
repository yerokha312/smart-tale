package dev.yerokha.smarttale.service;

import dev.yerokha.smarttale.dto.CustomPage;
import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.enums.ContextType;
import dev.yerokha.smarttale.repository.AdvertisementRepository;
import dev.yerokha.smarttale.repository.OrderRepository;
import dev.yerokha.smarttale.repository.OrganizationRepository;
import dev.yerokha.smarttale.repository.ProductRepository;
import dev.yerokha.smarttale.repository.PurchaseRepository;
import dev.yerokha.smarttale.repository.UserDetailsRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import static dev.yerokha.smarttale.mapper.CustomPageMapper.getCustomPage;
import static dev.yerokha.smarttale.service.TokenService.getOrgIdFromAuthToken;
import static dev.yerokha.smarttale.service.TokenService.getUserIdFromAuthToken;

@Service
public class SearchService {

    private final AdvertisementRepository advertisementRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrganizationRepository organizationRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final PurchaseRepository purchaseRepository;

    public SearchService(AdvertisementRepository advertisementRepository,
                         ProductRepository productRepository,
                         OrderRepository orderRepository,
                         OrganizationRepository organizationRepository,
                         UserDetailsRepository userDetailsRepository,
                         PurchaseRepository purchaseRepository) {
        this.advertisementRepository = advertisementRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.organizationRepository = organizationRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.purchaseRepository = purchaseRepository;
    }

    public CustomPage<SearchItem> search(Authentication authentication, String query, String context, boolean isDropDown, int page, int size) {
        Pageable pageable = getPageable(page, size, isDropDown);
        Long userId = getUserIdFromAuthToken(authentication);
        query = query == null ? "" : query.toLowerCase();
        Page<SearchItem> contentPage;
        if (context != null && !context.isEmpty()) {
            contentPage = switch (ContextType.valueOf(context.toUpperCase())) {
                case ADVERTISEMENT -> advertisementRepository.findSearchedItemsJPQL(query, null, pageable);
                case MY_ADVERTISEMENT -> advertisementRepository.findSearchedItemsJPQL(query, userId, pageable);
                case PRODUCT -> productRepository.findSearchedItemsJPQL(query, null, pageable);
                case MY_PRODUCT -> productRepository.findSearchedItemsJPQL(query, userId, pageable);
                case ORDER -> orderRepository.findSearchedItemsJPQL(query, null, pageable);
                case MY_ORDER -> orderRepository.findSearchedItemsJPQL(query, userId, pageable);
                case ORG_ORDER -> {
                    Long organizationId = getOrgIdFromAuthToken(authentication);
                    yield orderRepository.findSearchedItemsJPQL(query, organizationId, pageable, "org");
                }
                case ORGANIZATION -> organizationRepository.findSearchedItemsJPQL(query, pageable);
                case USER -> userDetailsRepository.findSearchedItemsJPQL(query, null, pageable);
                case EMPLOYEE -> {
                    Long organizationId = getOrgIdFromAuthToken(authentication);
                    yield userDetailsRepository.findSearchedItemsJPQL(query, organizationId, pageable);
                }
                case PURCHASE -> purchaseRepository.findSearchedItemsJPQL(query, userId, pageable);
            };
        } else {
            contentPage = Page.empty();
        }

        return getCustomPage(contentPage);
    }

    private Pageable getPageable(int page, int size, boolean isDropDown) {
        if (isDropDown) {
            page = 0;
        }

        return PageRequest.of(page, size);
    }
}
