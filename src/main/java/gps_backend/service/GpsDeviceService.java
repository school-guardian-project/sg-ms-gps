package gps_backend.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import gps_backend.model.GpsDevice;

@Service
public class GpsDeviceService {

    private final Map<String, GpsDevice> devices =
            new ConcurrentHashMap<>();

    public void register(String imei) {

        devices.putIfAbsent(
                imei,
                new GpsDevice(
                        imei,
                        true,
                        Instant.now()
                )
        );
    }

    public void registerDevice(String imei, java.net.Socket socket) {

        register(imei);
        updateConnection(imei);
    }

    public void updateConnection(String imei) {

        GpsDevice device = devices.get(imei);

        if (device == null) {
            register(imei);
            return;
        }

        device.setGpsStatus(true);
        device.setLastConnection(Instant.now());
    }

    public GpsDevice getDevice(String imei) {
        return devices.get(imei);
    }

    public boolean exists(String imei) {
        return devices.containsKey(imei);
    }
}