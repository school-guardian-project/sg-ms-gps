package gps_backend.tcp.protocol.handler;

import java.net.Socket;
import java.time.Instant;

import org.springframework.stereotype.Component;

import gps_backend.model.GpsLocation;
import gps_backend.parser.GpsPacketParser;
import gps_backend.service.GpsLocationService;
import gps_backend.tcp.protocol.GpsPacketHandler;
import gps_backend.tcp.protocol.ack.GpsAckService;

@Component
public class LocationPacketHandler implements GpsPacketHandler {

    private final GpsPacketParser packetParser;
    private final GpsLocationService locationService;
    private final GpsAckService ackService;

    public LocationPacketHandler(
            GpsPacketParser packetParser,
            GpsLocationService locationService,
            GpsAckService ackService) {
        this.packetParser = packetParser;
        this.locationService = locationService;
        this.ackService = ackService;
    }

    @Override
    public String handle(Socket socket, byte[] data, int length, String currentImei) {
        if (currentImei == null) {
            System.err.println("No hay IMEI asociado al GPS.");
        } else {
            GpsLocation location = packetParser.parseLocation(
                    data,
                    length,
                    currentImei,
                    Instant.now()
            );

            if (location != null) {
                locationService.save(location);
            }
        }

        ackService.sendAck(socket, data, length);
        return currentImei;
    }
}
