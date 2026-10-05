package gps_backend.parser;

import org.springframework.stereotype.Component;

import gps_backend.model.PositionType;

@Component
public class GpsPositionTypeDetector {

    public PositionType detect(byte[] data, int length) {
        int extensionStart = length - 9;
        int reUploadIndex = length - 7;

        if (extensionStart >= 22
                && length >= 31
                && isStandardFrameLength(data, length)) {
            int reUpload = data[reUploadIndex] & 0xFF;
            return reUpload == 0
                    ? PositionType.REAL_TIME
                    : PositionType.RE_UPLOAD;
        }

        return PositionType.UNKNOWN;
    }

    private boolean isStandardFrameLength(byte[] data, int length) {
        return length >= 5
                && (data[0] & 0xFF) == 0x78
                && (data[1] & 0xFF) == 0x78
                && ((data[2] & 0xFF) + 5) == length
                && (data[length - 2] & 0xFF) == 0x0D
                && (data[length - 1] & 0xFF) == 0x0A;
    }
}
