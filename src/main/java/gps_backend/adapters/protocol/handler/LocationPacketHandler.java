package gps_backend.adapters.protocol.handler;

import java.net.Socket;
import java.time.Instant;

import org.springframework.stereotype.Component;

import gps_backend.adapters.protocol.GpsPacketHandler;
import gps_backend.adapters.protocol.GpsPacketParser;
import gps_backend.adapters.protocol.ack.GpsAckService;
import gps_backend.application.service.GpsLocationApplicationService;
import gps_backend.domain.model.GpsLocation;

@Component
public class LocationPacketHandler implements GpsPacketHandler {

    private final GpsPacketParser packetParser;
    private final GpsLocationApplicationService locationService;
    private final GpsAckService ackService;

    public LocationPacketHandler(
            GpsPacketParser packetParser,
            GpsLocationApplicationService locationService,
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
