package gps_backend.tcp.protocol.ack;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.springframework.stereotype.Component;

@Component
public class GpsAckService {

    public void sendAck(Socket socket, byte[] data, int length) {
        try {
            byte[] ack = buildAck(data, length);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(ack);
            outputStream.flush();
            System.out.println("ACK enviado: " + bytesToHex(ack));
        } catch (IOException e) {
            System.err.println("Error enviando ACK: " + e.getMessage());
        }
    }

    public byte[] buildAck(byte[] data, int length) {
        int protocol = data[3] & 0xFF;
        int serial = getSerialNumber(data, length);
        byte[] ack = new byte[10];

        ack[0] = 0x78;
        ack[1] = 0x78;
        ack[2] = 0x05;
        ack[3] = (byte) protocol;
        ack[4] = (byte) ((serial >> 8) & 0xFF);
        ack[5] = (byte) (serial & 0xFF);

        int crc = calculateCrc(ack, 2, 6);
        ack[6] = (byte) ((crc >> 8) & 0xFF);
        ack[7] = (byte) (crc & 0xFF);
        ack[8] = 0x0D;
        ack[9] = 0x0A;

        return ack;
    }

    private int getSerialNumber(byte[] data, int length) {
        if (data == null || length < 6) {
            return 0;
        }

        int serialIndex = length - 6;
        int high = data[serialIndex] & 0xFF;
        int low = data[serialIndex + 1] & 0xFF;
        return (high << 8) | low;
    }

    private int calculateCrc(byte[] data, int start, int end) {
        int crc = 0;

        for (int i = start; i < end; i++) {
            crc ^= data[i] & 0xFF;

            for (int j = 0; j < 8; j++) {
                crc = (crc & 0x8000) != 0
                        ? (crc << 1) ^ 0x1021
                        : crc << 1;
                crc &= 0xFFFF;
            }
        }

        return crc;
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
