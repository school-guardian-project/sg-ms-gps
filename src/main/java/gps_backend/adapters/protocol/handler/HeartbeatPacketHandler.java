package gps_backend.adapters.protocol.handler;

import java.net.Socket;

import org.springframework.stereotype.Component;

import gps_backend.adapters.protocol.GpsPacketHandler;
import gps_backend.adapters.protocol.ack.GpsAckService;

@Component
public class HeartbeatPacketHandler implements GpsPacketHandler {

    private final GpsAckService ackService;

    public HeartbeatPacketHandler(GpsAckService ackService) {
        this.ackService = ackService;
    }

    @Override
    public String handle(Socket socket, byte[] data, int length, String currentImei) {
        System.out.println("Heartbeat recibido");
        ackService.sendAck(socket, data, length);
        return currentImei;
    }
}
