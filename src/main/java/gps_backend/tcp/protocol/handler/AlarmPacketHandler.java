package gps_backend.tcp.protocol.handler;

import java.net.Socket;
import java.time.Instant;

import org.springframework.stereotype.Component;

import gps_backend.parser.GpsPacketParser;
import gps_backend.tcp.protocol.GpsPacketHandler;
import gps_backend.tcp.protocol.ack.GpsAckService;

@Component
public class AlarmPacketHandler implements GpsPacketHandler {

    private final GpsPacketParser packetParser;
    private final GpsAckService ackService;

    public AlarmPacketHandler(GpsPacketParser packetParser, GpsAckService ackService) {
        this.packetParser = packetParser;
        this.ackService = ackService;
    }

    @Override
    public String handle(Socket socket, byte[] data, int length, String currentImei) {
        packetParser.parseAlarm(data, length, currentImei, Instant.now());
        ackService.sendAck(socket, data, length);
        return currentImei;
    }
}
