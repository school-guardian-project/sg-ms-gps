package gps_backend.tcp.protocol.handler;

import java.net.Socket;

import org.springframework.stereotype.Component;

import gps_backend.service.GpsDeviceService;
import gps_backend.tcp.protocol.GpsPacketHandler;
import gps_backend.tcp.protocol.ack.GpsAckService;

@Component
public class LoginPacketHandler implements GpsPacketHandler {

    private final GpsDeviceService deviceService;
    private final GpsAckService ackService;

    public LoginPacketHandler(GpsDeviceService deviceService, GpsAckService ackService) {
        this.deviceService = deviceService;
        this.ackService = ackService;
    }

    @Override
    public String handle(Socket socket, byte[] data, int length, String currentImei) {
        String imei = extractImei(data, length);
        System.out.println("GPS LOGIN - IMEI: " + imei);

        if (imei != null) {
            try {
                deviceService.registerDevice(imei, socket);
            } catch (Exception e) {
                System.err.println("No se pudo registrar GPS: " + e.getMessage());
            }
        }

        ackService.sendAck(socket, data, length);
        return imei;
    }

    private String extractImei(byte[] data, int length) {
        if (length < 12) {
            return null;
        }

        StringBuilder imei = new StringBuilder();
        for (int index = 4; index <= 11; index++) {
            imei.append(String.format("%02X", data[index] & 0xFF));
        }

        String result = imei.toString();
        return result.length() > 15 && result.startsWith("0")
                ? result.substring(1)
                : result;
    }
}
