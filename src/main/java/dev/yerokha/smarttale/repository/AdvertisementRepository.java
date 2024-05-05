package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.advertisement.Advertisement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {

    Page<Advertisement> findAllByPublishedByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Optional<Advertisement> findByPublishedByUserIdAndAdvertisementId(Long userId, Long advertisementId);

    @Modifying
    @Query("UPDATE Advertisement a SET a.views = a.views + 1 WHERE a.advertisementId = :advertisementId")
    void incrementViewsCount(Long advertisementId);
}
