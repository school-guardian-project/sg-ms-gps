package gps_backend.adapters.in.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gps_backend.domain.model.GpsLocation;
import gps_backend.application.service.GpsLocationApplicationService;

@RestController
@RequestMapping("/api/gps")
public class GpsLocationController {

    private final GpsLocationApplicationService gpsLocationService;

    public GpsLocationController(
            GpsLocationApplicationService gpsLocationService) {

        this.gpsLocationService = gpsLocationService;
    }

    @GetMapping("/devices/{imei}/location")
    public GpsLocation getLatestLocation(
            @PathVariable String imei) {

        return gpsLocationService.getLatest(imei);
    }

}
