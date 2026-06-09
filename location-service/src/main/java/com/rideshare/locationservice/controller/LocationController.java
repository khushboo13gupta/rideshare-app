package com.rideshare.locationservice.controller;

import com.rideshare.locationservice.dto.DriverLocationRequest;
import com.rideshare.locationservice.dto.NearByDriverResponse;
import com.rideshare.locationservice.service.DriverLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/locations")
@Slf4j
@RestController
public class LocationController {

    @Autowired
    private DriverLocationService driverLocationService;

    @PostMapping("/drivers/update")
    public ResponseEntity<String> updateDriverLocation( @RequestBody DriverLocationRequest driverLocationRequest) {
        // Implementation to update driver location
        driverLocationService.updateDriverLocation(driverLocationRequest);
        return ResponseEntity.ok("Driver location updated successfully");
    }

    @GetMapping("/drivers/nearby")
    public ResponseEntity<List<NearByDriverResponse>> getNearbyDrivers(@RequestParam double longitude,
                                                                       @RequestParam double latitude,
                                                                       @RequestParam (defaultValue = "5.0") double radiusInKm) {
        // Implementation to get nearby drivers

        return ResponseEntity.ok(driverLocationService.getNearbyDrivers(longitude, latitude, radiusInKm));
    }

    @DeleteMapping("/drivers/{driverId}")
    public ResponseEntity<String> deleteDriverLocation(@PathVariable String driverId) {
        // Implementation to delete driver location
        driverLocationService.deleteDriverLocation(driverId);
        return ResponseEntity.ok("Driver location deleted successfully");
    }

}
