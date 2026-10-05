package gps_backend.tcp.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

import org.springframework.stereotype.Component;

import gps_backend.tcp.protocol.GpsFrameDecoder;
import gps_backend.tcp.protocol.GpsPacketRouter;

@Component
public class GpsClientHandler {

    private final GpsPacketRouter packetRouter;
    private final GpsConnectionRegistry connectionRegistry;

    public GpsClientHandler(
            GpsPacketRouter packetRouter,
            GpsConnectionRegistry connectionRegistry) {
        this.packetRouter = packetRouter;
        this.connectionRegistry = connectionRegistry;
    }

    public void handle(Socket socket) {
        String currentImei = null;
        GpsFrameDecoder frameDecoder = new GpsFrameDecoder();

        try (InputStream inputStream = socket.getInputStream()) {
            byte[] buffer = new byte[4096];

            while (true) {
                int bytesRead = inputStream.read(buffer);
                if (bytesRead == -1) {
                    break;
                }
                if (bytesRead == 0) {
                    continue;
                }

                List<byte[]> frames = frameDecoder.feed(buffer, bytesRead);
                for (byte[] frame : frames) {
                    System.out.println("Datos recibidos (" + frame.length + " bytes):");
                    System.out.println(bytesToHex(frame));

                    String detectedImei = packetRouter.route(
                            socket,
                            frame,
                            frame.length,
                            currentImei
                    );

                    if (detectedImei != null) {
                        currentImei = detectedImei;
                        connectionRegistry.register(currentImei, socket);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error en conexión GPS: " + e.getMessage());
        } finally {
            connectionRegistry.remove(currentImei, socket);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            System.out.println("Conexión GPS cerrada: " + socket.getRemoteSocketAddress());
        }
    }

    private String bytesToHex(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte value : data) {
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(String.format("%02X", value & 0xFF));
        }
        return result.toString();
    }
}
