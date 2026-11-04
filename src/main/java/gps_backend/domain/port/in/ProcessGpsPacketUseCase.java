package gps_backend.domain.port.in;

import gps_backend.domain.model.GpsLocation;

/**
 * Interfaz de puerto de entrada que define el caso de uso para procesar paquetes GPS.
 * Recibe un IMEI y una ubicación, y es responsable de guardar la localización en el repositorio
 * y publicarla a Kafka para que otros servicios puedan consumirla.
 */
public interface ProcessGpsPacketUseCase {
    void saveLocation(String imei, GpsLocation location);
}
