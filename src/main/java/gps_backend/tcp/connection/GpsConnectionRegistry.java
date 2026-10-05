package gps_backend.tcp.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class GpsConnectionRegistry {

    private final Map<String, Socket> connections = new ConcurrentHashMap<>();

    public void register(String imei, Socket socket) {
        if (imei != null && socket != null) {
            connections.put(imei, socket);
        }
    }

    public void remove(String imei, Socket socket) {
        if (imei != null && socket != null) {
            connections.remove(imei, socket);
        }
    }

    public boolean isConnected(String imei) {
        Socket socket = connections.get(imei);
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    public boolean send(String imei, byte[] command) {
        Socket socket = connections.get(imei);

        if (!isConnected(imei)) {
            return false;
        }

        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(command);
            outputStream.flush();
            return true;
        } catch (IOException e) {
            System.err.println("Error enviando comando al GPS: " + e.getMessage());
            return false;
        }
    }
}
