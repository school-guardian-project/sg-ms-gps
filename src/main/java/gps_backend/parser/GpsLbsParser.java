package gps_backend.parser;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import gps_backend.model.GpsLbsData;
import gps_backend.model.GpsTimestampStatus;

@Component
public class GpsLbsParser {

    private static final ZoneId COLOMBIA_ZONE = ZoneId.of("America/Bogota");
    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public GpsLbsData parse(byte[] data, int length, String imei) {

        return parse(data, length, imei, null);
        }

        public GpsLbsData parse(
            byte[] data,
            int length,
            String imei,
            Instant receivedAt) {

        if (data == null || length < 22) {
            System.err.println("Paquete LBS demasiado corto.");
            return null;
        }

        int protocol = data[3] & 0xFF;

        if (protocol != 0x50) {
            System.err.println("El paquete no es protocolo 0x50.");
            return null;
        }

        System.out.println();
        System.out.println("========== ANALISIS 0x50 ==========");
        System.out.println("Longitud recibida: " + length);
        System.out.println();

        System.out.println("INDICE | HEX | DECIMAL");
        System.out.println("-----------------------");

        for (int i = 0; i < length; i++) {
            int value = data[i] & 0xFF;

            System.out.printf(
                    "%6d | %02X  | %7d%n",
                    i,
                    value,
                    value
            );
        }

        System.out.println("-----------------------------------");

        System.out.println();
        System.out.println("========== BYTES FECHA 0x50 ==========");
        for (int i = 4; i <= 9; i++) {
            System.out.printf(
                    "data[%d] = 0x%02X = %d%n",
                    i,
                    data[i] & 0xFF,
                    data[i] & 0xFF
            );
        }
        System.out.println("=======================================");
        try {
            OffsetDateTime dateTime = GpsPacketParser.parseGpsDate(data, 4, "0x50");

            System.out.println(
                    "GPS date 0x50 data[4..9]: " + dateTime + " (UTC)"
            );
            System.out.println(
                    "Colombia: " + dateTime.withOffsetSameInstant(ZoneOffset.of("-05:00"))
            );
            System.out.println(
                    "Importante: la fecha del protocolo 0x50 se conserva tal cual la reporta el GPS. "
                            + "No se aplica corrección manual de zona horaria a partir de la fecha del paquete."
            );

            int ta = data[10] & 0xFF;
            int mcc = readUnsignedShort(data, 11);
            int mnc = data[13] & 0xFF;

            int lac = readUnsignedShort(data, 14);
            long cellId = readUnsignedInt(data, 16);
            int signalStrength = data[20] & 0xFF;

            GpsTimestampStatus timestampStatus = GpsPacketParser.determineTimestampStatus(
                    dateTime.toInstant(),
                    receivedAt
            );

            GpsLbsData lbs = new GpsLbsData();

            lbs.setImei(imei);
            lbs.setDateTime(dateTime);
            lbs.setGpsDateTime(dateTime.toInstant());
            lbs.setReceivedAt(receivedAt);
            lbs.setTimestampStatus(timestampStatus);
            lbs.setTa(ta);
            lbs.setMcc(mcc);
            lbs.setMnc(mnc);
            lbs.setLac(lac);
            lbs.setCellId(cellId);
            lbs.setSignalStrength(signalStrength);
            lbs.setStatus("ACTIVE");

            System.out.println();
            System.out.println("========== LBS 0x50 ==========");
            System.out.println("IMEI: " + lbs.getImei());
            System.out.println("GPS date: " + lbs.getDateTime() + " (UTC)");
                System.out.println(
                    "Servidor received Colombia: "
                        + formatColombiaTime(lbs.getReceivedAt())
                );
            System.out.println("Timestamp status: " + lbs.getTimestampStatus());
            System.out.println("TA: " + lbs.getTa());
            System.out.println("MCC: " + lbs.getMcc());
            System.out.println("MNC: " + lbs.getMnc());
            System.out.println("LAC: " + lbs.getLac());
            System.out.println("Cell ID: " + lbs.getCellId());
            System.out.println("Signal: " + lbs.getSignalStrength());
            System.out.println("Estado: " + lbs.getStatus());
            System.out.println("==============================");

            return lbs;

        } catch (Exception e) {

            System.err.println(
                    "Error procesando paquete LBS 0x50: "
                    + e.getMessage()
            );

            return null;
        }
    }

    private int readUnsignedShort(byte[] data, int index) {

        return ((data[index] & 0xFF) << 8)
                | (data[index + 1] & 0xFF);
    }

    private long readUnsignedInt(byte[] data, int index) {

        return ((long) (data[index] & 0xFF) << 24)
                | ((long) (data[index + 1] & 0xFF) << 16)
                | ((long) (data[index + 2] & 0xFF) << 8)
                | (long) (data[index + 3] & 0xFF);
    }

    private String formatColombiaTime(Instant instant) {
        if (instant == null) {
            return "NO DISPONIBLE";
        }

        return instant.atZone(COLOMBIA_ZONE).format(DISPLAY_FORMAT);
    }
}