package gps_backend;

import static org.junit.jupiter.api.Assertions.assertSame;
import org.junit.jupiter.api.Test;

import gps_backend.application.service.GpsLocationApplicationService;
import gps_backend.domain.model.GpsLocation;
import gps_backend.domain.model.PositionType;

class GpsLocationServiceTests {

    private static final String IMEI = "355468590730586";

    @Test
    void shouldReplaceCurrentLocationWithNewRealTimeLocation() {
        GpsLocationApplicationService service = new GpsLocationApplicationService();
        GpsLocation first = location(PositionType.REAL_TIME);
        GpsLocation second = location(PositionType.REAL_TIME);

        service.save(first);
        service.save(second);

        assertSame(second, service.getLatest(IMEI));
    }

    @Test
    void shouldKeepRealTimeLocationWhenReUploadArrives() {
        GpsLocationApplicationService service = new GpsLocationApplicationService();
        GpsLocation realTime = location(PositionType.REAL_TIME);
        GpsLocation reUpload = location(PositionType.RE_UPLOAD);

        service.save(realTime);
        service.save(reUpload);

        assertSame(realTime, service.getLatest(IMEI));
    }

    @Test
    void shouldExposeInitialReUploadAsCurrentLocation() {
        GpsLocationApplicationService service = new GpsLocationApplicationService();
        GpsLocation reUpload = location(PositionType.RE_UPLOAD);

        service.save(reUpload);

        assertSame(reUpload, service.getLatest(IMEI));
    }

    @Test
    void shouldReplaceInitialReUploadWithRealTimeLocation() {
        GpsLocationApplicationService service = new GpsLocationApplicationService();
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
