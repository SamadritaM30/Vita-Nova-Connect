package com.medicalaidtracker.ambulance;

import com.medicalaidtracker.common.domain.DomainEnums.AmbulanceRequestStatus;
import com.medicalaidtracker.common.domain.TimestampedEntity;
import com.medicalaidtracker.hospital.Hospital;
import com.medicalaidtracker.user.PatientProfile;
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
@Table(name = "ambulance_requests")
public class AmbulanceRequest extends TimestampedEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(length = 36, columnDefinition = "char(36)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_profile_id", nullable = false)
    private PatientProfile patient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ambulance_id")
    private Ambulance ambulance;

    @Column(name = "pickup_address", nullable = false, length = 500)
    private String pickupAddress;

    @Column(name = "contact_number", nullable = false, length = 30)
    private String contactNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AmbulanceRequestStatus status = AmbulanceRequestStatus.PENDING;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;
}
