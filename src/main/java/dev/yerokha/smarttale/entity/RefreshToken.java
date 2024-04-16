package dev.yerokha.smarttale.entity;

import dev.yerokha.smarttale.entity.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "refresh_token")
@Data
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "token", unique = true, length = 1000)
    private String token;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    @Column(name = "issued_at")
    private Instant issuedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "is_revoked")
    private boolean isRevoked = false;

    public RefreshToken() {
    }

    public RefreshToken(String token, UserEntity userEntity, Instant issuedAt, Instant expiresAt) {
        this.token = token;
        this.userEntity = userEntity;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }
}