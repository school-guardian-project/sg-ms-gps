package gps_backend.tcp.protocol.handler;

import java.net.Socket;

import org.springframework.stereotype.Component;

import gps_backend.tcp.protocol.GpsPacketHandler;

@Component
public class UnknownPacketHandler implements GpsPacketHandler {

    @Override
    public String handle(Socket socket, byte[] data, int length, String currentImei) {
        if (length >= 2) {
            System.out.printf(
                    "Protocolo o header desconocido: %02X %02X%n",
                    data[0] & 0xFF,
                    data[1] & 0xFF
            );
        }
        return currentImei;
    }
}
