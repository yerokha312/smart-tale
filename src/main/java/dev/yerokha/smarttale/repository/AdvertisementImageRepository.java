package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.AdvertisementImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvertisementImageRepository extends JpaRepository<AdvertisementImage, Long> {
}
