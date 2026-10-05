package gps_backend.tcp.protocol;

import java.net.Socket;

public interface GpsPacketHandler {

    String handle(Socket socket, byte[] data, int length, String currentImei);
}
