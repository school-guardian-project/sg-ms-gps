package gps_backend.controller;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gps_backend.model.GpsLocation;
import gps_backend.service.GpsLocationService;

@RestController
@RequestMapping("/api/gps/test")
public class GpsTestController {

    private final GpsLocationService gpsLocationService;

    public GpsTestController(
            GpsLocationService gpsLocationService) {

        this.gpsLocationService =
                gpsLocationService;
    }

    @PostMapping("/{imei}")
    public GpsLocation createTestLocation(
            @PathVariable String imei) {

        GpsLocation location =
                new GpsLocation(
                        imei,
                        2.912000,
                        -75.307000,
                        19.5,
                        20,
                        OffsetDateTime.now(ZoneOffset.UTC),
                        "ACTIVE"
                );

        gpsLocationService.save(location);

        return location;
    }
}