package gps_backend.domain.model;

/**
 * Enumeración que clasifica el tipo de posición GPS reportada.
 * REAL_TIME: posición reportada en tiempo real cuando el GPS adquiere la señal.
 * RE_UPLOAD: posición histórica reenviada por el GPS (datos almacenados que se recargan).
 * UNKNOWN: tipo de posición no identificado en el paquete.
 */
public enum PositionType {
    REAL_TIME,
    RE_UPLOAD,
    UNKNOWN
}
