package gps_backend.application.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import gps_backend.domain.model.GpsDevice;

/**
 * Servicio de aplicación que gestiona el ciclo de vida de los dispositivos GPS.
 * Registra nuevos dispositivos, actualiza su estado de conexión y mantiene un registro de todos
 * los GPS conectados. Almacena los dispositivos en memoria con información de IMEI y último heartbeat.
 */
@Service
public class GpsDeviceApplicationService {

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
