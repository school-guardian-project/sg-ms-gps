package gps_backend.adapters.in.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import gps_backend.domain.model.GpsDevice;
import gps_backend.application.service.GpsDeviceApplicationService;

@RestController
@RequestMapping("/api/gps")
public class GpsDeviceController {

    private final GpsDeviceApplicationService gpsDeviceService;

    public GpsDeviceController(
            GpsDeviceApplicationService gpsDeviceService) {

        this.gpsDeviceService = gpsDeviceService;
    }

    @GetMapping("/devices/{imei}")
    public GpsDevice getDevice(
            @PathVariable String imei) {

        return gpsDeviceService.getDevice(imei);
    }

    @GetMapping("/devices/{imei}/connected")
    public boolean isConnected(
            @PathVariable String imei) {

        GpsDevice device =
                gpsDeviceService.getDevice(imei);

        return device != null
                && device.isGpsStatus();
    }
}
