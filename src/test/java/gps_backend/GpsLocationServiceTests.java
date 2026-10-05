package gps_backend;

import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

import gps_backend.model.GpsLocation;
import gps_backend.model.PositionType;
import gps_backend.service.GpsLocationService;

class GpsLocationServiceTests {

    private static final String IMEI = "355468590730586";

    @Test
    void shouldReplaceCurrentLocationWithNewRealTimeLocation() {
        GpsLocationService service = new GpsLocationService();
        GpsLocation first = location(PositionType.REAL_TIME);
        GpsLocation second = location(PositionType.REAL_TIME);

        service.save(first);
        service.save(second);

        assertSame(second, service.getLatest(IMEI));
    }

    @Test
    void shouldKeepRealTimeLocationWhenReUploadArrives() {
        GpsLocationService service = new GpsLocationService();
        GpsLocation realTime = location(PositionType.REAL_TIME);
        GpsLocation reUpload = location(PositionType.RE_UPLOAD);

        service.save(realTime);
        service.save(reUpload);

        assertSame(realTime, service.getLatest(IMEI));
    }

    @Test
    void shouldExposeInitialReUploadAsCurrentLocation() {
        GpsLocationService service = new GpsLocationService();
        GpsLocation reUpload = location(PositionType.RE_UPLOAD);

        service.save(reUpload);

        assertSame(reUpload, service.getLatest(IMEI));
    }

    @Test
    void shouldReplaceInitialReUploadWithRealTimeLocation() {
        GpsLocationService service = new GpsLocationService();
        GpsLocation reUpload = location(PositionType.RE_UPLOAD);
        GpsLocation realTime = location(PositionType.REAL_TIME);

        service.save(reUpload);
        service.save(realTime);

        assertSame(realTime, service.getLatest(IMEI));
    }

    private GpsLocation location(PositionType positionType) {
        GpsLocation location = new GpsLocation();
        location.setImei(IMEI);
        location.setPositionType(positionType);
        return location;
    }
}
