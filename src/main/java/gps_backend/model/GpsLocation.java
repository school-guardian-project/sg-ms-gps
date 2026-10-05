package gps_backend.model;

import java.time.Instant;
import java.time.OffsetDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GpsLocation {

    private String imei;
    private double latitude;
    private double longitude;
    private double speed;
    private double course;
    private OffsetDateTime dateTime;
    private Instant gpsDateTime;
    private Instant receivedAt;
    private GpsTimestampStatus timestampStatus;
    private String status;
    private PositionType positionType;

    public GpsLocation() {
    }

    public GpsLocation(
            String imei,
            double latitude,
            double longitude,
            double speed,
            double course,
            OffsetDateTime dateTime,
            String status) {

        this(
                imei,
                latitude,
                longitude,
                speed,
                course,
                dateTime,
                status,
                PositionType.UNKNOWN
        );
    }

    public GpsLocation(
            String imei,
            double latitude,
            double longitude,
            double speed,
            double course,
            OffsetDateTime dateTime,
            String status,
            PositionType positionType) {

        this.imei = imei;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.course = course;
        this.dateTime = dateTime;
        this.gpsDateTime = dateTime == null ? null : dateTime.toInstant();
        this.status = status;
        this.positionType = positionType;
    }

    @Override
    public String toString() {
        return "GpsLocation{" +
                "imei='" + imei + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", speed=" + speed +
                ", course=" + course +
                ", dateTime=" + dateTime +
                ", gpsDateTime=" + gpsDateTime +
                ", receivedAt=" + receivedAt +
                ", timestampStatus=" + timestampStatus +
                ", status='" + status + '\'' +
                ", positionType=" + positionType +
                '}';
    }
}