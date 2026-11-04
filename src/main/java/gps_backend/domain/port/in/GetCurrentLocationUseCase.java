package gps_backend.domain.port.in;

import gps_backend.domain.model.GpsLocation;
import java.util.Optional;

/**
 * Interfaz de puerto de entrada que define el caso de uso para obtener la ubicación actual.
 * Busca la última posición registrada para un IMEI específico sin necesidad de procesamiento adicional.
 * Típicamente utilizado por endpoints REST para consultar la ubicación de un GPS en tiempo real.
 */
public interface GetCurrentLocationUseCase {
    Optional<GpsLocation> getCurrentLocation(String imei);
}
