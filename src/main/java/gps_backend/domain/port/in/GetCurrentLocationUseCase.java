package gps_backend.domain.port.in;

import gps_backend.domain.model.GpsLocation;
import java.util.Optional;

public interface GetCurrentLocationUseCase {
    Optional<GpsLocation> getCurrentLocation(String imei);
}
