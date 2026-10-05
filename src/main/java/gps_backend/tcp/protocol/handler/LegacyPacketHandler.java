package gps_backend.tcp.protocol.handler;

import java.net.Socket;

import org.springframework.stereotype.Component;

import gps_backend.tcp.protocol.GpsPacketHandler;

@Component
public class LegacyPacketHandler implements GpsPacketHandler {

    @Override
    public String handle(Socket socket, byte[] data, int length, String currentImei) {
        if (length < 6) {
            System.out.println("Paquete 79 79 demasiado corto.");
            return currentImei;
        }

        int declaredLength = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
        int protocol = data[4] & 0xFF;
        System.out.printf("Paquete 79 79: longitud=%d, protocolo=0x%02X%n", declaredLength, protocol);

        if (protocol == 0x94 && length > 5) {
            int subtype = data[5] & 0xFF;
            System.out.printf("Protocolo 0x94, subtipo=0x%02X%n", subtype);
        }

        return currentImei;
    }
}
