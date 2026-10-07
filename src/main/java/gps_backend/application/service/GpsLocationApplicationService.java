package gps_backend.application.service;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import gps_backend.domain.model.GpsLocation;
import gps_backend.domain.model.PositionType;
import gps_backend.domain.port.in.GetCurrentLocationUseCase;
import gps_backend.domain.port.in.ProcessGpsPacketUseCase;

@Service
public class GpsLocationApplicationService
        implements GetCurrentLocationUseCase, ProcessGpsPacketUseCase {

    private final Map<String, GpsLocation> locations =
            new ConcurrentHashMap<>();

    // --- ProcessGpsPacketUseCase ---

    @Override
    public void saveLocation(String imei, GpsLocation location) {
        save(location);
    }

    // --- GetCurrentLocationUseCase ---

    @Override
    public Optional<GpsLocation> getCurrentLocation(String imei) {
        return Optional.ofNullable(getLatest(imei));
    }

    // --- métodos internos (misma lógica original) ---

    public void save(GpsLocation location) {

        locations.compute(
                location.getImei(),
                (imei, current) -> current != null
                        && current.getPositionType() == PositionType.REAL_TIME
                        && location.getPositionType() == PositionType.RE_UPLOAD
                        ? current
                        : location
        );

        System.out.println(
                "Ubicación guardada como "
                        + location.getPositionType()
                        + " para "
                        + location.getImei()
        );
    }

    public GpsLocation getLatest(String imei) {
        return locations.get(imei);
    }
}
