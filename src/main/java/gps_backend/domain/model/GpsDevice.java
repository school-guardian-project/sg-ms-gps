package gps_backend.domain.model;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

/**
 * Representa el estado de un dispositivo GPS conectado al sistema. Mantiene el IMEI único,
 * estado de conexión (activo/inactivo) y la última marca de tiempo de conexión.
 * Se actualiza cada vez que el dispositivo se conecta o envía un heartbeat.
 */
@Getter
@Setter
public class GpsDevice {

    private String imei;
    private boolean gpsStatus;
    private Instant lastConnection;

    public GpsDevice() {
    }

    public GpsDevice(
            String imei,
            boolean gpsStatus,
            Instant lastConnection) {

        this.imei = imei;
        this.gpsStatus = gpsStatus;
        this.lastConnection = lastConnection;
    }

    @Override
    public String toString() {
        return "GpsDevice{" +
                "imei='" + imei + '\'' +
                ", gpsStatus=" + gpsStatus +
                ", lastConnection=" + lastConnection +
                '}';
    }
}
