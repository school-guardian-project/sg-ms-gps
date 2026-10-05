package gps_backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gps_backend.model.GpsLocation;
import gps_backend.service.GpsLocationService;

@RestController
@RequestMapping("/api/gps")
public class GpsLocationController {

    private final GpsLocationService gpsLocationService;

    public GpsLocationController(
            GpsLocationService gpsLocationService) {

        this.gpsLocationService = gpsLocationService;
    }

    @GetMapping("/devices/{imei}/location")
    public GpsLocation getLatestLocation(
            @PathVariable String imei) {

        return gpsLocationService.getLatest(imei);
    }

}