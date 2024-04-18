package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findByHash(String hashString);

}
