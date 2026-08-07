package com.medicalaidtracker.representative;

import com.medicalaidtracker.common.domain.TimestampedEntity;
import com.medicalaidtracker.hospital.Hospital;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "representative_invitations")
public class RepresentativeInvitation extends TimestampedEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, columnDefinition = "char(36)")
    private UUID id;

    @Column(nullable = false, length = 254)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "employee_id", length = 80)
    private String employeeId;

    @Column(name = "organization_name", length = 160)
    private String organizationName;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(nullable = false)
    private boolean active = true;
}
