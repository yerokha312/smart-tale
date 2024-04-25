package dev.yerokha.smarttale.entity.user;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "invitations")
public class InvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invitation_id")
    private Long invitationId;

    @Column(name = "invited_at")
    private LocalDate invitedAt;

    @ManyToOne
    @JoinColumn(name = "inviter_id")
    private UserDetailsEntity inviter;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "invitee_id")
    private UserDetailsEntity invitee;

    @ManyToOne
    @JoinColumn(name = "organization_id")
    private OrganizationEntity organization;

    @OneToOne
    @JoinColumn(name = "position_id")
    private Position position;

    @Column(name = "accepted_at")
    private LocalDate acceptedAt;

    public InvitationEntity() {
    }

    public InvitationEntity(LocalDate invitedAt, UserDetailsEntity inviter, UserDetailsEntity invitee, OrganizationEntity organization, Position position) {
        this.invitedAt = invitedAt;
        this.inviter = inviter;
        this.invitee = invitee;
        this.organization = organization;
        this.position = position;
    }
}
