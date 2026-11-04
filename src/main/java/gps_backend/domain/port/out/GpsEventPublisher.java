package gps_backend.domain.port.out;

import gps_backend.adapters.kafka.event.GpsLocationEvent;

/**
 * Interfaz de puerto de salida que define el contrato para publicar eventos de ubicación GPS.
 * Cada vez que se procesa una ubicación, se publica un evento a través de este puerto (implementado vía Kafka)
 * para notificar a otros servicios del sistema (ej: sg-ms-notification) que hay una nueva localización.
 */
public interface GpsEventPublisher {
    void publishLocationReceived(GpsLocationEvent event);
}
