package gps_backend.parser;

import java.time.Instant;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

import gps_backend.model.GpsLocation;
import gps_backend.model.GpsTimestampStatus;
import gps_backend.model.PositionType;

@Component
public class GpsLocationDecoder {

    private final GpsDateParser dateParser;
    private final GpsTimestampService timestampService;
    private final GpsPositionTypeDetector positionTypeDetector;
    private final GpsPacketDebugPrinter debugPrinter;

    public GpsLocationDecoder(
            GpsDateParser dateParser,
            GpsTimestampService timestampService,
            GpsPositionTypeDetector positionTypeDetector,
            GpsPacketDebugPrinter debugPrinter) {
        this.dateParser = dateParser;
        this.timestampService = timestampService;
        this.positionTypeDetector = positionTypeDetector;
        this.debugPrinter = debugPrinter;
    }

    public GpsLocationDecoder() {
        this(
            new GpsDateParser(),
            new GpsTimestampService(),
            new GpsPositionTypeDetector(),
            new GpsPacketDebugPrinter()
        );
    }

    public GpsLocation decode(byte[] data, int length, String imei, Instant receivedAt) {
        if (data == null || length < 22 || (data[3] & 0xFF) != 0x31) {
            return null;
        }

        OffsetDateTime gpsDateTime = dateParser.parse(data, 4, "0x31");
        int satellites = data[10] & 0x0F;
        double latitude = readUnsignedInt(data, 11) / 1800000.0;
        double longitude = readUnsignedInt(data, 15) / 1800000.0;
        if (longitude > 0) {
            longitude = -longitude;
        }

        double speed = data[19] & 0xFF;
        int courseStatus = ((data[20] & 0xFF) << 8) | (data[21] & 0xFF);
        int course = courseStatus & 0x03FF;
        PositionType positionType = positionTypeDetector.detect(data, length);
        GpsTimestampStatus timestampStatus = timestampService.determineStatus(
                gpsDateTime.toInstant(),
                receivedAt
        );

        GpsLocation location = new GpsLocation();
        location.setImei(imei);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setSpeed(speed);
        location.setCourse(course);
        location.setDateTime(gpsDateTime);
        location.setGpsDateTime(gpsDateTime.toInstant());
        location.setReceivedAt(receivedAt);
        location.setTimestampStatus(timestampStatus);
        location.setStatus("ACTIVE");
        location.setPositionType(positionType);

        debugPrinter.printLocation(
            imei,
            gpsDateTime,
            latitude,
            longitude,
            speed,
            course,
            satellites,
            positionType,
            receivedAt,
            timestampStatus,
            timestampService
        );

        return location;
    }

    private long readUnsignedInt(byte[] data, int index) {
        return ((long) (data[index] & 0xFF) << 24)
                | ((long) (data[index + 1] & 0xFF) << 16)
                | ((long) (data[index + 2] & 0xFF) << 8)
                | (long) (data[index + 3] & 0xFF);
    }
}
