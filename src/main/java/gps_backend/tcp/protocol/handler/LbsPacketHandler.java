package gps_backend.tcp.protocol.handler;

import java.net.Socket;
import java.time.Instant;

import org.springframework.stereotype.Component;

import gps_backend.parser.GpsLbsParser;
import gps_backend.tcp.protocol.GpsPacketHandler;
import gps_backend.tcp.protocol.ack.GpsAckService;

@Component
public class LbsPacketHandler implements GpsPacketHandler {

    private final GpsLbsParser lbsParser;
    private final GpsAckService ackService;

    public LbsPacketHandler(GpsLbsParser lbsParser, GpsAckService ackService) {
        this.lbsParser = lbsParser;
        this.ackService = ackService;
    }

    @Override
    public String handle(Socket socket, byte[] data, int length, String currentImei) {
        lbsParser.parse(data, length, currentImei, Instant.now());
        ackService.sendAck(socket, data, length);
        return currentImei;
    }
}
