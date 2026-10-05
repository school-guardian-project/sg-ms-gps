package gps_backend.parser;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

@Component
public class GpsDateParser {

    public static final ZoneOffset VT03F_UTC = ZoneOffset.UTC;

    public OffsetDateTime parse(byte[] data, int startIndex, String protocolLabel) {
        if (data == null || data.length < startIndex + 6) {
            throw new IllegalArgumentException(
                    "Paquete GPS incompleto para fecha en " + protocolLabel
            );
        }

        int yy = decode(data[startIndex]);
        int mm = decode(data[startIndex + 1]);
        int dd = decode(data[startIndex + 2]);
        int hh = decode(data[startIndex + 3]);
        int mi = decode(data[startIndex + 4]);
        int ss = decode(data[startIndex + 5]);
        int year = 2000 + yy;

        try {
            return OffsetDateTime.of(year, mm, dd, hh, mi, ss, 0, VT03F_UTC);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Fecha GPS invalida para " + protocolLabel + ": "
                            + year + "-" + mm + "-" + dd + " "
                            + hh + ":" + mi + ":" + ss,
                    e
            );
        }
    }

    private int decode(byte value) {
        int decoded = value & 0xFF;
        if (decoded > 99) {
            throw new IllegalArgumentException(
                    "Byte de fecha GPS fuera de rango: 0x"
                            + String.format("%02X", value)
            );
        }
        return decoded;
    }
}
