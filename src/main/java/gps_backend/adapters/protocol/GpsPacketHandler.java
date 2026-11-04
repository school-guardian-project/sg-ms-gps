package gps_backend.adapters.protocol;

import java.net.Socket;

/**
 * Interfaz que define el contrato para todos los manejadores de paquetes GPS.
 * Cada tipo de protocolo (login, heartbeat, ubicación, etc.) implementa este contrato
 * para procesar su tipo específico de paquete.
 */
public interface GpsPacketHandler {

    String handle(Socket socket, byte[] data, int length, String currentImei);
}
