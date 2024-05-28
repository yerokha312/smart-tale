package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.dto.SearchItem;
import dev.yerokha.smarttale.entity.user.OrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    @Query("SELECT new dev.yerokha.smarttale.dto.SearchItem(" +
           "o.organizationId, " +
           "dev.yerokha.smarttale.enums.ContextType.ORGANIZATION, " +
           "o.name, " +
           "coalesce(i.imageUrl, '')" +
           ") " +
           "FROM OrganizationEntity o " +
           "LEFT JOIN Image i ON i.imageId = o.image.imageId " +
           "WHERE lower(o.name) LIKE %:query% " +
           "OR lower(o.description) LIKE %:query% " +
           "AND o.isDeleted = false")
    Page<SearchItem> findSearchedItemsJPQL(String query, Pageable pageable);
}
