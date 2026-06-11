package com.rideshare.rideservice.service;

import com.rideshare.rideservice.dto.RideRequest;
import com.rideshare.rideservice.dto.RideResponse;
import com.rideshare.rideservice.events.RideRequestedEvent;
import com.rideshare.rideservice.model.Ride;
import com.rideshare.rideservice.model.RideStatus;
import com.rideshare.rideservice.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;

    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;

    public RideResponse requestRide(final RideRequest rideRequest) {
        log.info("Processing ride request for rider: {}", rideRequest.getRiderId());
        // Implementation for requesting a ride
        Ride ride = new Ride();
        ride.setRiderId(rideRequest.getRiderId());
        ride.setPickUpLatitude(rideRequest.getPickupLatitude());
        ride.setPickUpLongitude(rideRequest.getPickupLongitude());
        ride.setPickUpAddress(rideRequest.getPickupAddress());
        ride.setDropOffLatitude(rideRequest.getDropLatitude());
        ride.setDropOffLongitude(rideRequest.getDropLongitude());
        ride.setDropOffAddress(rideRequest.getDropAddress());
        ride.setRideStatus(RideStatus.REQUESTED);
        ride.setEstimatedFare(calculateEstimateFare(rideRequest));

        // Save the ride to the repository
        Ride savedRide = rideRepository.save(ride);

        // Send the ride requested event to Kafka
        RideRequestedEvent event = new RideRequestedEvent();
        event.setRideId(ride.getId());
        event.setRiderId(ride.getRiderId());
        event.setPickupLatitude(ride.getPickUpLatitude());
        event.setPickupLongitude(ride.getPickUpLongitude());
        event.setPickupAddress(ride.getPickUpAddress());
        event.setDropLatitude(ride.getDropOffLatitude());
        event.setDropLongitude(ride.getDropOffLongitude());
        event.setDropAddress(ride.getDropOffAddress());

        kafkaTemplate.send("ride-requested", ride.getId(), event);
        log.info("RideRequestedEvent published to Kafka for ride: {}", ride.getId());

        savedRide.setRideStatus(RideStatus.MATCHING);
        rideRepository.save(savedRide);
        return mapToResponse(savedRide);

    }

    public void updateRideWithDriver(String rideId, String driverId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.setDriverId(driverId);
        ride.setRideStatus(RideStatus.ACCEPTED);
        rideRepository.save(ride);
    }

    public RideResponse startRide(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if(ride.getRideStatus() != RideStatus.ACCEPTED){
            throw new RuntimeException("Ride cannot be started. Current status: "+ride.getRideStatus());
        }

        ride.setRideStatus(RideStatus.RIDE_STARTED);
        ride.setStartedAt(LocalDateTime.now());
        rideRepository.save(ride);

        return mapToResponse(ride);
    }

    public RideResponse completeRide(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        if(ride.getRideStatus() != RideStatus.RIDE_STARTED){
            throw new RuntimeException("Ride cannot be completed. Current status: "+ride.getRideStatus());
        }
        ride.setRideStatus(RideStatus.COMPLETED);
        ride.setCompletedAt(LocalDateTime.now());
        ride.setActualFare(ride.getEstimatedFare());
        rideRepository.save(ride);

        return mapToResponse(ride);

    }

    public RideResponse cancelRide(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));

        ride.setRideStatus(RideStatus.CANCELLED);
        rideRepository.save(ride);
        return mapToResponse(ride);
    }

    public RideResponse getRideById(String rideId){
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Ride not found"));
        return mapToResponse(ride);
    }

    public List<RideResponse> getRidesByRider(String riderId){
        return rideRepository.findByRiderIdOrderByCreatedAtDesc(riderId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private double calculateEstimateFare(RideRequest request){
        // Simplified Haversine distance calculation
        double lat1 = Math.toRadians(request.getPickupLatitude());
        double lat2 = Math.toRadians(request.getDropLatitude());

        double lon1 = Math.toRadians(request.getPickupLongitude());
        double lon2 = Math.toRadians(request.getDropLongitude());

        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a =Math.pow(Math.sin(dLat / 2), 2)
                +Math.cos(lat1) * Math.cos(lat2)
                *Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double dustanceKm = 6371 * c;

        //Base fare: 50Rs + 12Rs. perKm
        double fare = 50 + (dustanceKm * 12);
        return Math.round(fare * 100.0) / 100.0;
    }

    private RideResponse mapToResponse(final Ride ride) {

        RideResponse response = new RideResponse();
        response.setId(ride.getId());
        response.setRiderId(ride.getRiderId());
        response.setDriverId(ride.getDriverId());
        response.setPickupLatitude(ride.getPickUpLatitude());
        response.setPickupLongitude(ride.getPickUpLongitude());
        response.setPickupAddress(ride.getPickUpAddress());
        response.setDropLatitude(ride.getDropOffLatitude());
        response.setDropLongitude(ride.getDropOffLongitude());
        response.setDropAddress(ride.getDropOffAddress());
        response.setStatus(ride.getRideStatus());
        response.setEstimatedFare(ride.getEstimatedFare());
        response.setActualFare(ride.getActualFare());
        response.setCreatedAt(ride.getCreatedAt());
        response.setStartedAt(ride.getStartedAt());
        response.setCompletedAt(ride.getCompletedAt());
        return response;
    }
}
