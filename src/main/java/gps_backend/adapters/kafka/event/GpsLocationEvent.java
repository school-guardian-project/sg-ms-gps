package gps_backend.adapters.kafka.event;

import java.time.Instant;

/**
 * Evento que se publica a Kafka cuando se procesa una ubicación GPS. Encapsula
 * toda la información de localización en un formato serializable (JSON) que otros servicios
 * pueden consumir. Es el modelo de datos que viaja a través de Kafka.
 */
public class GpsLocationEvent {
    public String eventId;
    public String timestamp;
    public GpsLocationEventData data;

    public GpsLocationEvent() {}

    public GpsLocationEvent(String eventId, String timestamp, GpsLocationEventData data) {
        this.eventId = eventId;
        this.timestamp = timestamp;
        this.data = data;
    }

    public static class GpsLocationEventData {
        public String imei;
        public double latitude;
        public double longitude;
        public double speed;
        public double course;
        public String gpsDateTime;
        public String receivedAt;
        public String timestampStatus;
        public String positionType;

        public GpsLocationEventData() {}

        public GpsLocationEventData(
                String imei, double latitude, double longitude, double speed, double course,
                String gpsDateTime, String receivedAt, String timestampStatus, String positionType) {
            this.imei = imei;
            this.latitude = latitude;
            this.longitude = longitude;
            this.speed = speed;
            this.course = course;
            this.gpsDateTime = gpsDateTime;
            this.receivedAt = receivedAt;
            this.timestampStatus = timestampStatus;
            this.positionType = positionType;
        }
    }
}
