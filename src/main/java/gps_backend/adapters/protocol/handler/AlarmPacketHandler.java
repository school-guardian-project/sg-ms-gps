package gps_backend.adapters.protocol.handler;

import java.net.Socket;
import java.time.Instant;

import org.springframework.stereotype.Component;

import gps_backend.adapters.protocol.GpsPacketHandler;
import gps_backend.adapters.protocol.GpsPacketParser;
import gps_backend.adapters.protocol.ack.GpsAckService;

/**
 * Manejador de paquetes de alarma (protocolo 0x32). Procesa eventos especiales detectados por el GPS
 * como robo, salida de zona, velocidad excesiva, etc. Imprime los detalles para logging y análisis.
 * Responde con ACK al dispositivo.
 */
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
