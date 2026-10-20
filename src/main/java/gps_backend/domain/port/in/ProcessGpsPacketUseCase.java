package gps_backend.domain.port.in;

import gps_backend.domain.model.GpsLocation;

public interface ProcessGpsPacketUseCase {
    void saveLocation(String imei, GpsLocation location);
}
