package gps_backend;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import gps_backend.tcp.protocol.GpsFrameDecoder;

class GpsFrameDecoderTests {

    @Test
    void shouldWaitForACompleteFrameWhenTcpSplitsIt() {
        GpsFrameDecoder decoder = new GpsFrameDecoder();
        byte[] frame = heartbeatFrame();

        assertEquals(0, decoder.feed(frame, 5).size());

        List<byte[]> decoded = decoder.feed(
                java.util.Arrays.copyOfRange(frame, 5, frame.length),
                frame.length - 5
        );

        assertEquals(1, decoded.size());
        assertEquals(frame.length, decoded.get(0).length);
    }

    @Test
    void shouldReturnEveryFrameWhenTcpCombinesSeveralFrames() {
        GpsFrameDecoder decoder = new GpsFrameDecoder();
        byte[] first = heartbeatFrame();
        byte[] second = heartbeatFrame();
        byte[] combined = new byte[first.length + second.length];

        System.arraycopy(first, 0, combined, 0, first.length);
        System.arraycopy(second, 0, combined, first.length, second.length);

        List<byte[]> decoded = decoder.feed(combined, combined.length);

        assertEquals(2, decoded.size());
        assertEquals(first.length, decoded.get(0).length);
        assertEquals(second.length, decoded.get(1).length);
    }

    private byte[] heartbeatFrame() {
        return new byte[] {
                0x78, 0x78, 0x05, 0x13,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x0D, 0x0A
        };
    }
}
