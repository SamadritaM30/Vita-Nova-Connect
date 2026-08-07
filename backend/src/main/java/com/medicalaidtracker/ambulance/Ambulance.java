package com.medicalaidtracker.ambulance;

import com.medicalaidtracker.common.domain.DomainEnums.AmbulanceStatus;
import com.medicalaidtracker.common.domain.TimestampedEntity;
import com.medicalaidtracker.hospital.Hospital;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "ambulances")
public class Ambulance extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 30)
    private String vehicleNumber;

    @Column(name = "driver_name", nullable = false, length = 120)
    private String driverName;

    @Column(name = "contact_number", nullable = false, length = 30)
    private String contactNumber;

    @Column(name = "last_location", nullable = false, length = 255)
    private String lastLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AmbulanceStatus status = AmbulanceStatus.AVAILABLE;

    @Version
    @Column(nullable = false)
    private long version;
}
