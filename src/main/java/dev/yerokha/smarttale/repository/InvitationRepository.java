package dev.yerokha.smarttale.repository;

import dev.yerokha.smarttale.entity.user.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {
    boolean existsByInviteeEmail(String email);
}