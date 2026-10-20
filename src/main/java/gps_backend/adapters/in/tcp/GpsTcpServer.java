package gps_backend.adapters.in.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gps_backend.adapters.protocol.GpsLbsParser;
import gps_backend.adapters.protocol.GpsPacketParser;
import gps_backend.adapters.protocol.ack.GpsAckService;
import gps_backend.application.service.GpsDeviceApplicationService;
import gps_backend.application.service.GpsLocationApplicationService;

@Component
public class GpsTcpServer {

    private static final int PORT = 8842;

    private final GpsClientHandler clientHandler;
    private final GpsConnectionRegistry connectionRegistry;
    private final GpsAckService ackService;

    @Autowired
    public GpsTcpServer(
            GpsClientHandler clientHandler,
            GpsConnectionRegistry connectionRegistry,
            GpsAckService ackService) {
        this.clientHandler = clientHandler;
        this.connectionRegistry = connectionRegistry;
        this.ackService = ackService;
    }

    public GpsTcpServer(
            GpsLocationApplicationService locationService,
            GpsDeviceApplicationService deviceService,
            GpsPacketParser packetParser,
            GpsLbsParser lbsParser) {
        this.clientHandler = null;
        this.connectionRegistry = new GpsConnectionRegistry();
        this.ackService = new GpsAckService();
    }

    public void start() {
        Thread serverThread = new Thread(this::runServer, "gps-tcp-server");
        serverThread.start();
    }

    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println();
            System.out.println("=================================");
            System.out.println("   GPS TCP SERVER INICIADO");
            System.out.println("   Puerto: " + PORT);
            System.out.println("=================================");
            System.out.println();

            while (!Thread.currentThread().isInterrupted()) {
                Socket socket = serverSocket.accept();
                System.out.println("GPS conectado desde: " + socket.getRemoteSocketAddress());

                Thread clientThread = new Thread(
                        () -> clientHandler.handle(socket),
                        "gps-client-" + socket.getPort()
                );
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Error iniciando servidor TCP: " + e.getMessage());
        }
    }

    public boolean isGpsConnected(String imei) {
        return connectionRegistry.isConnected(imei);
    }

    public boolean sendToGps(String imei, byte[] command) {
        return connectionRegistry.send(imei, command);
    }

    @SuppressWarnings("unused")
    private byte[] buildAck(byte[] data, int length) {
        return ackService.buildAck(data, length);
    }
}
