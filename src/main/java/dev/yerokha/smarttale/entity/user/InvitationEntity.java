package dev.yerokha.smarttale.entity.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@Entity
@Table(name = "invitations")
public class InvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long invitationId;

    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    @ManyToOne
    @JoinColumn(name = "inviter_id")
    private UserDetailsEntity inviter;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "invitee_id")
    private UserDetailsEntity invitee;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @ManyToOne
    @JoinColumn(name = "position_id")
    private PositionEntity position;

    public InvitationEntity() {
    }

    public InvitationEntity(LocalDateTime invitedAt, UserDetailsEntity inviter, UserDetailsEntity invitee, OrganizationEntity organization, PositionEntity position) {
        this.invitedAt = invitedAt;
        this.inviter = inviter;
        this.invitee = invitee;
        this.organization = organization;
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvitationEntity that = (InvitationEntity) o;
        return Objects.equals(invitee.getUserId(), that.invitee.getUserId())
               && Objects.equals(position.getPositionId(), that.position.getPositionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(invitee.getUserId(), position.getPositionId());
    }
}
