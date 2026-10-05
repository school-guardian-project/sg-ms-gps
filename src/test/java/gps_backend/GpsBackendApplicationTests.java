package gps_backend;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import gps_backend.model.GpsLbsData;
import gps_backend.model.GpsLocation;
import gps_backend.model.GpsTimestampStatus;
import gps_backend.parser.GpsLbsParser;
import gps_backend.parser.GpsPacketParser;
import gps_backend.tcp.GpsTcpServer;

@SpringBootTest
class GpsBackendApplicationTests {

    @Test
    void contextLoads() {
    }

    @Test
    void gpsPacketParserShouldDecodeVt03fTimestampAsUtc() {
        GpsPacketParser parser = new GpsPacketParser();
        byte[] packet = new byte[40];
        packet[0] = 0x78;
        packet[1] = 0x78;
        packet[2] = 0x28;
        packet[3] = 0x31;
        packet[4] = 0x1A;
        packet[5] = 0x09;
        packet[6] = 0x15;
        packet[7] = 0x14;
        packet[8] = 0x1A;
        packet[9] = 0x10;
        packet[10] = 0x05;
        packet[38] = 0x0D;
        packet[39] = 0x0A;

        GpsLocation location = parser.parseLocation(packet, packet.length, "123456789012345");

        assertNotNull(location);
        assertEquals(OffsetDateTime.of(2026, 9, 21, 20, 26, 16, 0, ZoneOffset.UTC), location.getDateTime());
        assertEquals(2026, location.getDateTime().getYear());
    }

    @Test
    void gpsPacketParserShouldDecodeAlarmTimestampAsUtc() {
        GpsPacketParser parser = new GpsPacketParser();
        byte[] packet = new byte[40];
        packet[0] = 0x78;
        packet[1] = 0x78;
        packet[2] = 0x27;
        packet[3] = 0x32;
        packet[4] = 0x1A;
        packet[5] = 0x09;
        packet[6] = 0x16;
        packet[7] = 0x0D;
        packet[8] = 0x14;
        packet[9] = 0x29;
        packet[38] = 0x0D;
        packet[39] = 0x0A;

        parser.parseAlarm(packet, packet.length, "355468590730586");

        OffsetDateTime gpsDate = GpsPacketParser.parseGpsDate(packet, 4, "0x32");

        assertEquals(OffsetDateTime.of(2026, 9, 22, 13, 20, 41, 0, ZoneOffset.UTC), gpsDate);
        assertEquals(OffsetDateTime.of(2026, 9, 22, 8, 20, 41, 0, ZoneOffset.of("-05:00")), gpsDate.atZoneSameInstant(ZoneOffset.of("-05:00")).toOffsetDateTime());
    }

    @Test
    void gpsLbsParserShouldKeepProtocolDateWithoutManualTimezoneShift() {
        GpsLbsParser parser = new GpsLbsParser();
        byte[] packet = new byte[22];
        packet[0] = 0x78;
        packet[1] = 0x78;
        packet[2] = 0x37;
        packet[3] = 0x50;
        packet[4] = 0x1A;
        packet[5] = 0x09;
        packet[6] = 0x16;
        packet[7] = 0x12;
        packet[8] = 0x1B;
        packet[9] = 0x38;
        packet[20] = 0x00;

        GpsLbsData location = parser.parse(packet, packet.length, "123456789012345");

        assertNotNull(location);
        assertEquals(OffsetDateTime.of(2026, 9, 22, 18, 27, 56, 0, ZoneOffset.UTC), location.getDateTime());
    }

    @Test
    void gpsTcpServerShouldBuildAckForAlarmPacket() throws Exception {
        GpsTcpServer server = new GpsTcpServer(null, null, null, null);
        byte[] packet = new byte[40];
        packet[0] = 0x78;
        packet[1] = 0x78;
        packet[2] = 0x27;
        packet[3] = 0x32;
        packet[4] = 0x1A;
        packet[5] = 0x09;
        packet[6] = 0x16;
        packet[7] = 0x0D;
        packet[8] = 0x14;
        packet[9] = 0x29;
        packet[38] = 0x0D;
        packet[39] = 0x0A;

        Method buildAck = GpsTcpServer.class.getDeclaredMethod("buildAck", byte[].class, int.class);
        buildAck.setAccessible(true);
        byte[] ack = (byte[]) buildAck.invoke(server, packet, packet.length);

        assertEquals(0x78, ack[0] & 0xFF);
        assertEquals(0x78, ack[1] & 0xFF);
        assertEquals(0x32, ack[3] & 0xFF);
        assertEquals(0x0D, ack[8] & 0xFF);
        assertEquals(0x0A, ack[9] & 0xFF);
    }

    @Test
    void gpsTcpServerShouldUseTheRealSerialForGpsPositionAck() throws Exception {
        GpsTcpServer server = new GpsTcpServer(null, null, null, null);
        byte[] packet = new byte[] {
                (byte) 0x78, (byte) 0x78, (byte) 0x28, (byte) 0x31,
                (byte) 0x1A, (byte) 0x09, (byte) 0x16, (byte) 0x0D,
                (byte) 0x14, (byte) 0x29, (byte) 0xC0, (byte) 0x00,
                (byte) 0x51, (byte) 0x63, (byte) 0xC9, (byte) 0x08,
                (byte) 0x13, (byte) 0xCB, (byte) 0x7B, (byte) 0x00,
                (byte) 0x0C, (byte) 0xAD, (byte) 0x02, (byte) 0xDC,
                (byte) 0x00, (byte) 0x65, (byte) 0x56, (byte) 0xB9,
                (byte) 0x00, (byte) 0x1B, (byte) 0x5C, (byte) 0x65,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x02, (byte) 0xE4, (byte) 0x1E, (byte) 0x0D,
                (byte) 0x0A
        };

        Method buildAck = GpsTcpServer.class.getDeclaredMethod(
                "buildAck",
                byte[].class,
                int.class
        );
        buildAck.setAccessible(true);

        byte[] ack = (byte[]) buildAck.invoke(server, packet, packet.length);

        assertEquals(0x00, ack[4] & 0xFF);
        assertEquals(0x02, ack[5] & 0xFF);
    }

    @Test
    void gpsTcpServerShouldBuildAckForLbsPacket() throws Exception {
        GpsTcpServer server = new GpsTcpServer(null, null, null, null);
        byte[] packet = new byte[22];
        packet[0] = 0x78;
        packet[1] = 0x78;
        packet[2] = 0x37;
        packet[3] = 0x50;
        packet[4] = 0x1A;
        packet[5] = 0x09;
        packet[6] = 0x16;
        packet[7] = 0x12;
        packet[8] = 0x1B;
        packet[9] = 0x38;
        packet[20] = 0x00;

        Method buildAck = GpsTcpServer.class.getDeclaredMethod("buildAck", byte[].class, int.class);
        buildAck.setAccessible(true);
        byte[] ack = (byte[]) buildAck.invoke(server, packet, packet.length);

        assertEquals(0x78, ack[0] & 0xFF);
        assertEquals(0x78, ack[1] & 0xFF);
        assertEquals(0x50, ack[3] & 0xFF);
        assertTrue(ack.length >= 10);
    }

    @Test
    void gpsPacketParserShouldParseLocationDataWithoutLosingDateMetadata() {
        GpsPacketParser parser = new GpsPacketParser();
        byte[] packet = new byte[40];
        packet[0] = 0x78;
        packet[1] = 0x78;
        packet[2] = 0x28;
        packet[3] = 0x31;
        packet[4] = 0x1A;
        packet[5] = 0x09;
        packet[6] = 0x16;
        packet[7] = 0x0D;
        packet[8] = 0x14;
        packet[9] = 0x29;
        packet[10] = 0x06;
        packet[11] = 0x00;
        packet[12] = 0x00;
        packet[13] = 0x00;
        packet[14] = 0x00;
        packet[15] = 0x00;
        packet[16] = 0x00;
        packet[17] = 0x00;
        packet[18] = 0x00;
        packet[19] = 0x00;
        packet[20] = 0x00;
        packet[21] = 0x00;
        packet[38] = 0x0D;
        packet[39] = 0x0A;

        GpsLocation location = parser.parseLocation(
            packet,
            packet.length,
            "355468590730586",
            Instant.parse("2026-09-22T13:20:42Z")
        );

        assertNotNull(location);
        assertEquals(OffsetDateTime.of(2026, 9, 22, 13, 20, 41, 0, ZoneOffset.UTC), location.getDateTime());
        assertNotNull(location.getReceivedAt());
        assertEquals(0.0, location.getLatitude());
        assertEquals(0.0, location.getLongitude());
        assertEquals(0.0, location.getSpeed());
        assertEquals(6, packet[10] & 0x0F);
    }

    @Test
    void gpsPacketParserShouldUseTheReceiveInstantProvidedByTheServer() {
        GpsPacketParser parser = new GpsPacketParser();
        byte[] packet = new byte[40];
        packet[0] = 0x78;
        packet[1] = 0x78;
        packet[2] = 0x28;
        packet[3] = 0x31;
        packet[4] = 0x1A;
        packet[5] = 0x09;
        packet[6] = 0x16;
        packet[7] = 0x0D;
        packet[8] = 0x14;
        packet[9] = 0x29;
        packet[10] = 0x06;
        packet[38] = 0x0D;
        packet[39] = 0x0A;

        Instant receivedAt = Instant.parse("2026-09-22T13:20:42Z");
        GpsLocation location = parser.parseLocation(
                packet,
                packet.length,
                "355468590730586",
                receivedAt
        );

        assertNotNull(location);
        assertEquals(receivedAt, location.getReceivedAt());
        assertEquals(GpsTimestampStatus.VALID, location.getTimestampStatus());
        assertEquals(
                Instant.parse("2026-09-22T13:20:41Z"),
                location.getGpsDateTime()
        );
    }

    @Test
    void gpsTcpServerShouldBuildAckForLoginPacket() throws Exception {
        GpsTcpServer server = new GpsTcpServer(null, null, null, null);
        byte[] packet = new byte[24];
        packet[0] = 0x78;
        packet[1] = 0x78;
        packet[2] = 0x12;
        packet[3] = 0x01;
        packet[4] = 0x00;
        packet[5] = 0x00;
        packet[6] = 0x00;
        packet[7] = 0x00;
        packet[8] = 0x00;
        packet[9] = 0x00;
        packet[10] = 0x00;
        packet[11] = 0x00;
        packet[22] = 0x0D;
        packet[23] = 0x0A;

        Method buildAck = GpsTcpServer.class.getDeclaredMethod("buildAck", byte[].class, int.class);
        buildAck.setAccessible(true);
        byte[] ack = (byte[]) buildAck.invoke(server, packet, packet.length);

        assertEquals(0x01, ack[3] & 0xFF);
        assertEquals(0x0D, ack[8] & 0xFF);
        assertEquals(0x0A, ack[9] & 0xFF);
    }

    @Test
    void gpsTcpServerShouldBuildAckForHeartbeatPacket() throws Exception {
        GpsTcpServer server = new GpsTcpServer(null, null, null, null);
        byte[] packet = new byte[12];
        packet[0] = 0x78;
        packet[1] = 0x78;
        packet[2] = 0x05;
        packet[3] = 0x13;
        packet[10] = 0x0D;
        packet[11] = 0x0A;

        Method buildAck = GpsTcpServer.class.getDeclaredMethod("buildAck", byte[].class, int.class);
        buildAck.setAccessible(true);
        byte[] ack = (byte[]) buildAck.invoke(server, packet, packet.length);

        assertEquals(0x13, ack[3] & 0xFF);
        assertEquals(0x78, ack[0] & 0xFF);
        assertEquals(0x78, ack[1] & 0xFF);
    }
}
