package gps_backend.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import gps_backend.model.GpsLocation;
import gps_backend.model.PositionType;

@Service
public class GpsLocationService {

    private final Map<String, GpsLocation> locations =
            new ConcurrentHashMap<>();

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