package gps_backend.domain.port.out;

import gps_backend.domain.model.GpsLocation;
import java.util.Optional;

/**
 * Interfaz de puerto de salida para guardar ubicaciones GPS. Define el contrato que debe implementar
 * el repositorio para persistir o almacenar las posiciones. Desacopla la lógica de negocio
 * de la estrategia de almacenamiento (en memoria, base de datos, etc.).
 */
public interface GpsLocationRepository {
    void save(String imei, GpsLocation location);
    Optional<GpsLocation> findLatestByImei(String imei);
}
