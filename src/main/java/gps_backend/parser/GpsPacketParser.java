package gps_backend.parser;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gps_backend.model.GpsLocation;
import gps_backend.model.GpsTimestampStatus;

@Component
public class GpsPacketParser {

    public static final ZoneOffset VT03F_UTC = ZoneOffset.UTC;

    private final GpsLocationDecoder locationDecoder;
    private final GpsAlarmDecoder alarmDecoder;

    @Autowired
    public GpsPacketParser(
            GpsLocationDecoder locationDecoder,
            GpsAlarmDecoder alarmDecoder) {
        this.locationDecoder = locationDecoder;
        this.alarmDecoder = alarmDecoder;
    }

    public GpsPacketParser() {
        this(
                new GpsLocationDecoder(),
                new GpsAlarmDecoder()
        );
    }

    public GpsLocation parseLocation(byte[] data, int length, String imei) {
        return parseLocation(data, length, imei, null);
    }

    public GpsLocation parseLocation(
            byte[] data,
            int length,
            String imei,
            Instant receivedAt) {
        return locationDecoder.decode(data, length, imei, receivedAt);
    }

    public void parseAlarm(byte[] data, int length, String imei) {
        parseAlarm(data, length, imei, null);
    }

    public void parseAlarm(
            byte[] data,
            int length,
            String imei,
            Instant receivedAt) {
        alarmDecoder.decode(data, length, imei, receivedAt);
    }

    public static OffsetDateTime parseGpsDate(
            byte[] data,
            int startIndex,
            String protocolLabel) {
        return new GpsDateParser().parse(data, startIndex, protocolLabel);
    }

    public static GpsTimestampStatus determineTimestampStatus(
            Instant gpsDateTime,
            Instant receivedAt) {
        return new GpsTimestampService().determineStatus(gpsDateTime, receivedAt);
    }
}
