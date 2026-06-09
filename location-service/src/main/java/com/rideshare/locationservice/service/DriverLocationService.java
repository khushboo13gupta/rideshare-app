package com.rideshare.locationservice.service;

import com.rideshare.locationservice.dto.DriverLocationRequest;
import com.rideshare.locationservice.dto.NearByDriverResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class DriverLocationService {

    private final RedisTemplate<String, String> redisTemplate;

    public static final String DRIVER_LOCATION_KEY = "driver:location";

    public void updateDriverLocation(final DriverLocationRequest driverLocationRequest) {
        log.info("Updating location for driverId: {}", driverLocationRequest.getDriverId());

        Point point = new Point(driverLocationRequest.getLongitude(), driverLocationRequest.getLatitude());

        redisTemplate.opsForGeo().add(DRIVER_LOCATION_KEY, point, driverLocationRequest.getDriverId());

        log.info("Driver location updated in Redis for driverId: {}", driverLocationRequest.getDriverId());

    }

    public List<NearByDriverResponse> getNearbyDrivers(final double longitude,
                                                       final double latitude,
                                                       final double radiusInKm) {
        log.info("Fetching nearby drivers for location: ({}, {}) with radius: {} km", longitude, latitude, radiusInKm);
        Circle searchArea = new Circle(new Point(longitude, latitude),
                new Distance(radiusInKm, Metrics.KILOMETERS));
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        DRIVER_LOCATION_KEY,
                        searchArea,
                        RedisGeoCommands.GeoRadiusCommandArgs
                                .newGeoRadiusArgs()
                                .includeCoordinates()
                                .includeDistance()
                                .sortAscending().limit(10));

        List<NearByDriverResponse> nearByDrivers = Collections.emptyList();
        if (results != null) {
            nearByDrivers = results.getContent().stream()
                    .map(result -> new NearByDriverResponse(
                            result.getContent().getName(),
                            result.getContent().getPoint().getY(),
                            result.getContent().getPoint().getX(),
                            result.getDistance().getValue()))
                    .toList();
        }
        log.info("Found {} nearby drivers", nearByDrivers.size());
        return nearByDrivers;
    }

    public void deleteDriverLocation(final String driverId) {
        log.info("Deleting location for driverId: {}", driverId);
        redisTemplate.opsForGeo().remove(DRIVER_LOCATION_KEY, driverId);
    }
}
