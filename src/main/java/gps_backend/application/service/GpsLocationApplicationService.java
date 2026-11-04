package gps_backend.application.service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import gps_backend.adapters.kafka.event.GpsLocationEvent;
import gps_backend.domain.model.GpsLocation;
import gps_backend.domain.model.PositionType;
import gps_backend.domain.port.in.GetCurrentLocationUseCase;
import gps_backend.domain.port.in.ProcessGpsPacketUseCase;
import gps_backend.domain.port.out.GpsEventPublisher;

/**
 * Servicio de aplicación que gestiona ubicaciones GPS y su publicación. Recibe ubicaciones procesadas,
 * las almacena en memoria, y las publica a Kafka para que otros servicios (notificaciones, alertas, etc.)
 * puedan consumirlas en tiempo real. Implementa dos interfaces de negocio: procesar ubicaciones y obtenerlas.
 */
@Service
public class GpsLocationApplicationService
        implements GetCurrentLocationUseCase, ProcessGpsPacketUseCase {

    private final Map<String, GpsLocation> locations =
            new ConcurrentHashMap<>();
    private final GpsEventPublisher eventPublisher;

    public GpsLocationApplicationService(GpsEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

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

        // Publicar evento a Kafka
        publishLocationEvent(location);
    }

    public GpsLocation getLatest(String imei) {
        return locations.get(imei);
    }

    private void publishLocationEvent(GpsLocation location) {
        try {
            GpsLocationEvent event = new GpsLocationEvent(
                    UUID.randomUUID().toString(),
                    Instant.now().toString(),
                    new GpsLocationEvent.GpsLocationEventData(
                            location.getImei(),
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getSpeed(),
                            location.getCourse(),
                            location.getGpsDateTime().toString(),
                            location.getReceivedAt().toString(),
                            location.getTimestampStatus().toString(),
                            location.getPositionType().toString()
                    )
            );

            eventPublisher.publishLocationReceived(event);
        } catch (Exception e) {
            System.err.println("Failed to publish GPS location event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
