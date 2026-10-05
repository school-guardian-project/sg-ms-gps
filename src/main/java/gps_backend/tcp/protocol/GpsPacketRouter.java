package gps_backend.tcp.protocol;

import java.net.Socket;

import org.springframework.stereotype.Component;

import gps_backend.tcp.protocol.handler.AlarmPacketHandler;
import gps_backend.tcp.protocol.handler.CommandPacketHandler;
import gps_backend.tcp.protocol.handler.HeartbeatPacketHandler;
import gps_backend.tcp.protocol.handler.LbsPacketHandler;
import gps_backend.tcp.protocol.handler.LegacyPacketHandler;
import gps_backend.tcp.protocol.handler.LocationPacketHandler;
import gps_backend.tcp.protocol.handler.LoginPacketHandler;
import gps_backend.tcp.protocol.handler.UnknownPacketHandler;

@Component
public class GpsPacketRouter {

    private final GpsPacketHandler loginHandler;
    private final GpsPacketHandler heartbeatHandler;
    private final GpsPacketHandler locationHandler;
    private final GpsPacketHandler alarmHandler;
    private final GpsPacketHandler lbsHandler;
    private final GpsPacketHandler commandHandler;
    private final GpsPacketHandler legacyHandler;
    private final GpsPacketHandler unknownHandler;

    public GpsPacketRouter(
            LoginPacketHandler loginHandler,
            HeartbeatPacketHandler heartbeatHandler,
            LocationPacketHandler locationHandler,
            AlarmPacketHandler alarmHandler,
            LbsPacketHandler lbsHandler,
            CommandPacketHandler commandHandler,
            LegacyPacketHandler legacyHandler,
            UnknownPacketHandler unknownHandler) {
        this.loginHandler = loginHandler;
        this.heartbeatHandler = heartbeatHandler;
        this.locationHandler = locationHandler;
        this.alarmHandler = alarmHandler;
        this.lbsHandler = lbsHandler;
        this.commandHandler = commandHandler;
        this.legacyHandler = legacyHandler;
        this.unknownHandler = unknownHandler;
    }

    public String route(Socket socket, byte[] data, int length, String currentImei) {
        if (data == null || length < 5) {
            return currentImei;
        }

        int first = data[0] & 0xFF;
        int second = data[1] & 0xFF;

        if (first == 0x79 && second == 0x79) {
            return legacyHandler.handle(socket, data, length, currentImei);
        }

        if (first != 0x78 || second != 0x78) {
            return unknownHandler.handle(socket, data, length, currentImei);
        }

        return handlerFor(data[3] & 0xFF)
                .handle(socket, data, length, currentImei);
    }

    private GpsPacketHandler handlerFor(int protocol) {
        return switch (protocol) {
            case 0x01 -> loginHandler;
            case 0x13 -> heartbeatHandler;
            case 0x31 -> locationHandler;
            case 0x32 -> alarmHandler;
            case 0x50 -> lbsHandler;
            case 0x80 -> commandHandler;
            default -> unknownHandler;
        };
    }
}
