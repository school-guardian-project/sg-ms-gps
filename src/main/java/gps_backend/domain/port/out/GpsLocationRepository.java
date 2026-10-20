package gps_backend.domain.port.out;

import gps_backend.domain.model.GpsLocation;
import java.util.Optional;

public interface GpsLocationRepository {
    void save(String imei, GpsLocation location);
    Optional<GpsLocation> findLatestByImei(String imei);
}
