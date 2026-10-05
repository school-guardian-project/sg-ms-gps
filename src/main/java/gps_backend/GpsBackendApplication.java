package gps_backend;

import gps_backend.tcp.GpsTcpServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsBackendApplication implements CommandLineRunner {

    private final GpsTcpServer gpsTcpServer;

    public GpsBackendApplication(
            GpsTcpServer gpsTcpServer) {

        this.gpsTcpServer = gpsTcpServer;
    }

    public static void main(String[] args) {

        SpringApplication.run(
                GpsBackendApplication.class,
                args
        );
    }

    @Override
    public void run(String... args) {

        gpsTcpServer.start();
    }
}