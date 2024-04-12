package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("SELECT rt FROM RefreshToken rt WHERE rt.userEntity.email = :email AND rt.isRevoked = false")
    List<RefreshToken> findNotRevokedByEmail(String email);

}
