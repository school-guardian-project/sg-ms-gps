package gps_backend.adapters.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import gps_backend.adapters.kafka.event.GpsLocationEvent;
import gps_backend.domain.port.out.GpsEventPublisher;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Implementación del publicador de eventos que utiliza Apache Kafka. Recibe eventos de ubicación GPS,
 * los serializa a JSON, y los envía al tema "gps.location.received" para que servicios como
 * sg-ms-notification puedan consumirlos y procesar alertas o notificaciones.
 */
@Component
public class KafkaGpsEventPublisher implements GpsEventPublisher {

    private static final String TOPIC = "gps.location.received";
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaGpsEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publishLocationReceived(GpsLocationEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(TOPIC, event.data.imei, payload);
            System.out.println("Published GPS location event to Kafka: " + event.data.imei);
        } catch (Exception e) {
            System.err.println("Failed to publish GPS location event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
