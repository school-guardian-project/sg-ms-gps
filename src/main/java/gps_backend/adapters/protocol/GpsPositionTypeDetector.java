package gps_backend.adapters.protocol;

import org.springframework.stereotype.Component;

import gps_backend.domain.model.PositionType;

/**
 * Detector del tipo de posición GPS: REAL_TIME vs RE_UPLOAD. Examina la estructura del paquete
 * (longitud, headers, footers) y un byte específico de extensión para determinar si es una
 * ubicación actual o datos históricos reenviados por el dispositivo.
 */
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
