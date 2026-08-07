package com.medicalaidtracker.bed;

import com.medicalaidtracker.common.domain.DomainEnums.BedStatus;
import com.medicalaidtracker.common.domain.DomainEnums.WardType;
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
@Table(name = "beds")
public class Bed extends TimestampedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(name = "bed_code", nullable = false, length = 40)
    private String bedCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "ward_type", nullable = false, length = 20)
    private WardType wardType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BedStatus status = BedStatus.AVAILABLE;

    @Version
    @Column(nullable = false)
    private long version;
}
