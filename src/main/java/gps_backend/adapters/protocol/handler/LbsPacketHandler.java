package gps_backend.adapters.protocol.handler;

import java.net.Socket;
import java.time.Instant;

import org.springframework.stereotype.Component;

import gps_backend.adapters.protocol.GpsLbsParser;
import gps_backend.adapters.protocol.GpsPacketHandler;
import gps_backend.adapters.protocol.ack.GpsAckService;

/**
 * Manejador de paquetes LBS (protocolo 0x50). Procesa datos de localización basados en torres celulares
 * que complementan la información GPS. Útil cuando el satélite no es accesible pero la red móvil sí.
 * Extrae información de operador celular (MCC, MNC) e identificadores de celda.
 */
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
