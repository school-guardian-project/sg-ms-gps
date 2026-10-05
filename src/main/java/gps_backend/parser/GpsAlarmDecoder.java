package gps_backend.parser;

import java.time.Instant;
import java.time.OffsetDateTime;

import org.springframework.stereotype.Component;

@Component
public class GpsAlarmDecoder {

    private final GpsDateParser dateParser;
    private final GpsTimestampService timestampService;
    private final GpsPacketDebugPrinter debugPrinter;

    public GpsAlarmDecoder(
            GpsDateParser dateParser,
            GpsTimestampService timestampService,
            GpsPacketDebugPrinter debugPrinter) {
        this.dateParser = dateParser;
        this.timestampService = timestampService;
        this.debugPrinter = debugPrinter;
    }

    public GpsAlarmDecoder() {
        this(new GpsDateParser(), new GpsTimestampService(), new GpsPacketDebugPrinter());
    }

    public void decode(byte[] data, int length, String imei, Instant receivedAt) {
        if (data == null || length < 10 || (data[3] & 0xFF) != 0x32) {
            return;
        }

        OffsetDateTime gpsTimestamp = dateParser.parse(data, 4, "0x32");
        debugPrinter.printAlarm(
                data,
                length,
                imei,
                gpsTimestamp,
                receivedAt,
                timestampService.determineStatus(gpsTimestamp.toInstant(), receivedAt),
                timestampService
        );
    }
}
