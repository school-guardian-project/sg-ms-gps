package gps_backend;

import gps_backend.adapters.in.tcp.GpsTcpServer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal de la aplicación GPS. Inicia el servidor TCP que escucha
 * conexiones de dispositivos GPS físicos. La aplicación Spring Boot se ejecuta y
 * automáticamente inicia el servidor de TCP en el puerto 8842.
 */
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