package gps_backend.domain.model;

import java.time.Instant;
import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * Almacena datos de localización basados en señales de torres celulares (LBS: Location Based Services).
 * Se extrae del paquete de protocolo 0x50 y contiene información de red móvil (MCC, MNC, LAC, Cell ID)
 * que complementan la localización GPS cuando esta no está disponible.
 */
@Getter
@Setter
public class GpsLbsData {

    private String imei;
    private OffsetDateTime dateTime;
    private Instant gpsDateTime;
    private Instant receivedAt;
    private GpsTimestampStatus timestampStatus;

    private int ta;
    private int mcc;
    private int mnc;

    private int lac;
    private long cellId;
    private int signalStrength;

    private String status;

    @Override
    public String toString() {
        return "GpsLbsData{" +
                "imei='" + imei + '\'' +
                ", dateTime=" + dateTime +
                ", gpsDateTime=" + gpsDateTime +
                ", receivedAt=" + receivedAt +
                ", timestampStatus=" + timestampStatus +
                ", ta=" + ta +
                ", mcc=" + mcc +
                ", mnc=" + mnc +
                ", lac=" + lac +
                ", cellId=" + cellId +
                ", signalStrength=" + signalStrength +
                ", status='" + status + '\'' +
                '}';
    }
}
