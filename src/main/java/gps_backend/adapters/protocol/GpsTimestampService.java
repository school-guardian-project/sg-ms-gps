package gps_backend.adapters.protocol;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import gps_backend.domain.model.GpsTimestampStatus;

/**
 * Servicio que determina la validez del timestamp GPS. Compara la fecha reportada por el GPS
 * con la hora de servidor. Si la diferencia es mayor a 300 segundos (5 minutos), marca como STALE.
 * También proporciona utilidades para formatear fechas en la zona horaria de Colombia.
 */
@Component
public class GpsTimestampService {

    private static final ZoneId COLOMBIA_ZONE = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public GpsTimestampStatus determineStatus(Instant gpsDateTime, Instant receivedAt) {
        if (gpsDateTime == null || receivedAt == null) {
            return GpsTimestampStatus.INVALID;
        }

        long seconds = Math.abs(Duration.between(gpsDateTime, receivedAt).getSeconds());
        return seconds <= 300
                ? GpsTimestampStatus.VALID
                : GpsTimestampStatus.STALE;
    }

    public String formatColombiaTime(Instant instant) {
        if (instant == null) {
            return "NO DISPONIBLE";
        }
        return instant.atZone(COLOMBIA_ZONE).format(DISPLAY_FORMAT);
    }
}
