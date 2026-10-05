package gps_backend.tcp.protocol;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GpsFrameDecoder {

    private static final int FRAME_HEADER_SIZE = 4;
    private static final int FRAME_FOOTER_SIZE = 2;
    private static final int MAX_FRAME_SIZE = 8192;

    private final ByteArrayOutputStream pending = new ByteArrayOutputStream();

    public synchronized List<byte[]> feed(byte[] data, int length) {
        if (data == null || length <= 0) {
            return List.of();
        }

        pending.write(data, 0, length);
        List<byte[]> frames = new ArrayList<>();

        while (true) {
            byte[] buffer = pending.toByteArray();
            int headerIndex = findHeader(buffer);

            if (headerIndex < 0) {
                keepPossibleHeaderPrefix(buffer);
                break;
            }

            if (headerIndex > 0) {
                discard(headerIndex);
                buffer = pending.toByteArray();
            }

            if (buffer.length < FRAME_HEADER_SIZE) {
                break;
            }

            int minimumLength = declaredMinimumLength(buffer);
            int frameEnd = findFooter(buffer, minimumLength);

            if (frameEnd < 0) {
                if (buffer.length > MAX_FRAME_SIZE) {
                    discard(1);
                    continue;
                }
                break;
            }

            int frameLength = frameEnd + FRAME_FOOTER_SIZE;
            frames.add(Arrays.copyOf(buffer, frameLength));
            discard(frameLength);
        }

        return frames;
    }

    private int findHeader(byte[] buffer) {
        for (int index = 0; index < buffer.length - 1; index++) {
            int first = buffer[index] & 0xFF;
            int second = buffer[index + 1] & 0xFF;
            if ((first == 0x78 || first == 0x79) && first == second) {
                return index;
            }
        }
        return -1;
    }

    private int declaredMinimumLength(byte[] buffer) {
        if ((buffer[0] & 0xFF) == 0x78) {
            return (buffer[2] & 0xFF) + 5;
        }

        int declaredLength = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);
        return declaredLength + 6;
    }

    private int findFooter(byte[] buffer, int minimumLength) {
        int start = Math.max(FRAME_HEADER_SIZE, minimumLength) - FRAME_FOOTER_SIZE;

        for (int index = start; index < buffer.length - 1; index++) {
            if ((buffer[index] & 0xFF) == 0x0D
                    && (buffer[index + 1] & 0xFF) == 0x0A) {
                return index;
            }
        }

        return -1;
    }

    private void keepPossibleHeaderPrefix(byte[] buffer) {
        if (buffer.length > 0) {
            int last = buffer[buffer.length - 1] & 0xFF;
            if (last == 0x78 || last == 0x79) {
                pending.reset();
                pending.write(buffer[buffer.length - 1]);
                return;
            }
        }
        pending.reset();
    }

    private void discard(int count) {
        byte[] remaining = pending.toByteArray();
        pending.reset();
        if (count < remaining.length) {
            pending.write(remaining, count, remaining.length - count);
        }
    }
}
