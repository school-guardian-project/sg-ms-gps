package gps_backend.parser;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import gps_backend.model.GpsTimestampStatus;
import gps_backend.model.PositionType;

@Component
public class GpsPacketDebugPrinter {

    public void printLocation(
            String imei,
            OffsetDateTime dateTime,
            double latitude,
            double longitude,
            double speed,
            int course,
            int satellites,
            PositionType positionType,
            Instant receivedAt,
            GpsTimestampStatus timestampStatus,
            GpsTimestampService timestampService) {
        System.out.println("\n========== GPS 0x31 ==========");
        System.out.println("IMEI: " + imei);
        System.out.println("GPS date: " + dateTime + " (UTC)");
        System.out.println("Colombia: " + dateTime.withOffsetSameInstant(ZoneOffset.of("-05:00")));
        System.out.println("Server received Colombia: "
                + timestampService.formatColombiaTime(receivedAt));
        System.out.println("Timestamp status: " + timestampStatus);
        System.out.printf("Latitud: %.6f%n", latitude);
        System.out.printf("Longitud: %.6f%n", longitude);
        System.out.println("Velocidad: " + speed + " km/h");
        System.out.println("Curso: " + course + " grados");
        System.out.println("Satelites: " + satellites);
        System.out.println("Tipo de posicion: " + positionType);
        System.out.println("==================================");
    }

    public void printAlarm(
            byte[] data,
            int length,
            String imei,
            OffsetDateTime gpsTimestamp,
            Instant receivedAt,
            GpsTimestampStatus timestampStatus,
            GpsTimestampService timestampService) {
        System.out.println("\n========== GPS ALARMA ==========");
        System.out.println("IMEI: " + imei);
        System.out.println("Longitud recibida: " + length);
        System.out.println("\nPaquete hexadecimal:");
        printHex(data, length);
        System.out.println("\nINDICE | HEX | DECIMAL");
        for (int index = 0; index < length; index++) {
            int value = data[index] & 0xFF;
            System.out.printf("%5d | %02X  | %7d%n", index, value, value);
        }
        System.out.println("Protocolo: 0x32");
        System.out.println("GPS date: " + gpsTimestamp + " (UTC)");
        System.out.println("Colombia: "
            + gpsTimestamp.withOffsetSameInstant(ZoneOffset.of("-05:00")));
        System.out.println("Server received Colombia: "
                + timestampService.formatColombiaTime(receivedAt));
        System.out.println("Timestamp status: " + timestampStatus);
        System.out.println("================================");
    }

    public void printUnknownPositionType() {
        System.out.println("Tipo de posicion: DESCONOCIDO");
    }

    private void printHex(byte[] data, int length) {
        for (int index = 0; index < length; index++) {
            System.out.printf("%02X ", data[index] & 0xFF);
        }
        System.out.println();
    }
}
