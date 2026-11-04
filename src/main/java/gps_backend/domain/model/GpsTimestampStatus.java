package gps_backend.domain.model;

/**
 * Enumeración que indica la validez de la marca de tiempo GPS.
 * VALID: la fecha GPS está dentro de 300 segundos de la fecha del servidor (confiable).
 * STALE: la fecha GPS está fuera del rango (posible retraso o error del dispositivo).
 * INVALID: los datos de fecha no son válidos.
 */
public enum GpsTimestampStatus {
    VALID,
    STALE,
    INVALID
}
