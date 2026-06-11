package com.rideshare.rideservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String driverId;

    @Column(nullable = false)
    private String riderId;

    @Column(nullable = false)
    private double pickUpLatitude;
    @Column(nullable = false)
    private double pickUpLongitude;

    @Column(nullable = false)
    private double dropOffLatitude;

    @Column(nullable = false)
    private double dropOffLongitude;

    @Column(nullable = false)
    private String pickUpAddress;

    @Column(nullable = false)
    private String dropOffAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RideStatus rideStatus;

    private double estimatedFare;
    private double actualFare;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;





}
