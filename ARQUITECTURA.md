# Arquitectura del backend GPS

## 1. Proposito

Este proyecto es un backend Spring Boot para recibir datos de dispositivos GPS por TCP, interpretar sus paquetes, guardar ubicaciones en memoria y exponer la informacion mediante una API REST.

Actualmente soporta:

- Conexion TCP de dispositivos GPS.
- Registro de dispositivos por IMEI.
- Paquetes de login (`0x01`).
- Paquetes heartbeat (`0x13`).
- Paquetes de ubicacion (`0x31`).
- Paquetes de alarma (`0x32`).
- Paquetes LBS o informacion de red movil (`0x50`).
- Paquetes de comandos (`0x80`).
- Paquetes con header `79 79`.
- Construccion y envio de ACK.
- Calculo de CRC.
- Reconstruccion de tramas TCP fragmentadas o concatenadas.
- Consulta de dispositivo, ultima ubicacion e historial mediante HTTP.

La aplicacion usa almacenamiento en memoria. Todavia no hay persistencia real en SQL Server, aunque las dependencias de JPA y el driver de SQL Server ya estan declarados en Maven.

---

## 2. Estructura general

```text
src/
├── main/
│   ├── java/gps_backend/
│   │   ├── GpsBackendApplication.java
│   │   ├── controller/
│   │   │   ├── GpsDeviceController.java
│   │   │   ├── GpsLocationController.java
│   │   │   └── GpsTestController.java
│   │   ├── model/
│   │   │   ├── GpsDevice.java
│   │   │   ├── GpsLocation.java
│   │   │   ├── GpsLbsData.java
│   │   │   ├── GpsTimestampStatus.java
│   │   │   └── PositionType.java
│   │   ├── parser/
│   │   │   ├── GpsAlarmDecoder.java
│   │   │   ├── GpsDateParser.java
│   │   │   ├── GpsLocationDecoder.java
│   │   │   ├── GpsLbsParser.java
│   │   │   ├── GpsPacketDebugPrinter.java
│   │   │   ├── GpsPacketParser.java
│   │   │   ├── GpsPositionTypeDetector.java
│   │   │   └── GpsTimestampService.java
│   │   ├── service/
│   │   │   ├── GpsDeviceService.java
│   │   │   └── GpsLocationService.java
│   │   └── tcp/
│   │       ├── GpsTcpServer.java
│   │       ├── connection/
│   │       │   ├── GpsClientHandler.java
│   │       │   └── GpsConnectionRegistry.java
│   │       └── protocol/
│   │           ├── GpsFrameDecoder.java
│   │           ├── GpsPacketHandler.java
│   │           ├── GpsPacketRouter.java
│   │           ├── ack/
│   │           │   └── GpsAckService.java
│   │           └── handler/
│   │               ├── AlarmPacketHandler.java
│   │               ├── CommandPacketHandler.java
│   │               ├── HeartbeatPacketHandler.java
│   │               ├── LbsPacketHandler.java
│   │               ├── LegacyPacketHandler.java
│   │               ├── LocationPacketHandler.java
│   │               ├── LoginPacketHandler.java
│   │               └── UnknownPacketHandler.java
│   └── resources/
│       ├── application.properties
│       ├── static/
│       └── templates/
└── test/
    └── java/gps_backend/
        ├── GpsBackendApplicationTests.java
        └── GpsFrameDecoderTests.java
```

La idea principal es que cada paquete tenga una responsabilidad concreta:

| Paquete | Responsabilidad |
|---|---|
| `controller` | Recibir peticiones HTTP y devolver respuestas REST. |
| `model` | Representar dispositivos, ubicaciones, datos LBS y estados. |
| `parser` | Convertir bytes de los paquetes GPS en objetos Java. |
| `service` | Administrar el estado y los datos recibidos. |
| `tcp` | Iniciar el servidor TCP. |
| `tcp.connection` | Administrar sockets y clientes conectados. |
| `tcp.protocol` | Reconstruir, identificar y enrutar paquetes. |
| `tcp.protocol.ack` | Construir y enviar respuestas ACK. |
| `tcp.protocol.handler` | Procesar cada tipo de protocolo GPS. |

---

## 3. Punto de entrada de la aplicacion

### `GpsBackendApplication.java`

Es la clase principal de Spring Boot.

Responsabilidades:

- Arrancar Spring Boot mediante `SpringApplication.run`.
- Implementar `CommandLineRunner`.
- Iniciar `GpsTcpServer` cuando la aplicacion termina de levantar el contexto.

Flujo simplificado:

```text
Spring Boot inicia
        |
        v
GpsBackendApplication.run()
        |
        v
GpsTcpServer.start()
        |
        v
Servidor TCP escuchando en el puerto 8842
```

Esta clase no interpreta paquetes ni administra directamente sockets.

---

## 4. Capa HTTP: controllers

Todos los controllers usan la ruta base `/api/gps`.

### `GpsDeviceController.java`

Expone informacion del dispositivo GPS.

Endpoints:

```text
GET /api/gps/devices/{imei}
GET /api/gps/devices/{imei}/connected
```

Responsabilidades:

- Recibir el IMEI desde la URL.
- Consultar `GpsDeviceService`.
- Devolver el dispositivo o su estado de conexion.

No contiene logica de sockets ni interpreta paquetes TCP.

### `GpsLocationController.java`

Expone la ubicacion mas reciente y el historial.

Endpoints:

```text
GET /api/gps/devices/{imei}/location
GET /api/gps/devices/{imei}/history
```

Responsabilidades:

- Recibir el IMEI.
- Consultar `GpsLocationService`.
- Devolver un `GpsLocation` o una lista de ubicaciones.

### `GpsTestController.java`

Crea manualmente una ubicacion de prueba.

Endpoint:

```text
POST /api/gps/test/{imei}
```

Responsabilidades:

- Crear una ubicacion fija de prueba.
- Guardarla mediante `GpsLocationService`.
- Devolverla en la respuesta HTTP.

Este controller sirve para probar el flujo REST sin depender de un GPS fisico.

---

## 5. Modelos

Los modelos representan los datos que circulan por el sistema. Usan Lombok para generar getters y setters.

### `GpsDevice.java`

Representa un GPS registrado.

Campos principales:

- `imei`: identificador unico del dispositivo.
- `gpsStatus`: indica si el dispositivo esta conectado.
- `lastConnection`: instante de la ultima conexion.

### `GpsLocation.java`

Representa una ubicacion GPS.

Campos principales:

- `imei`: GPS que envio la ubicacion.
- `latitude`: latitud.
- `longitude`: longitud.
- `speed`: velocidad.
- `course`: direccion o rumbo.
- `dateTime`: fecha reportada por el dispositivo.
- `gpsDateTime`: fecha del GPS convertida a `Instant`.
- `receivedAt`: instante en que el servidor recibio el paquete.
- `timestampStatus`: resultado de comparar las dos fechas.
- `status`: estado de la ubicacion, actualmente `ACTIVE`.
- `positionType`: indica si es tiempo real, reenvio o desconocida.

El sistema conserva dos referencias de tiempo importantes:

```text
GPS date       = fecha enviada por el dispositivo
receivedAt     = momento real de recepcion en el servidor
```

### `GpsLbsData.java`

Representa informacion de red movil enviada por el protocolo LBS `0x50`.

Incluye:

- Fecha del GPS y fecha de recepcion.
- `ta`: timing advance.
- `mcc`: codigo de pais movil.
- `mnc`: codigo de red movil.
- `lac`: location area code.
- `cellId`: identificador de celda.
- `signalStrength`: intensidad de senal.
- Estado del registro.

### `GpsTimestampStatus.java`

Enumera el estado de una fecha GPS respecto al servidor:

```text
VALID   -> diferencia de hasta 5 minutos
STALE   -> diferencia superior a 5 minutos
INVALID -> falta la fecha del GPS o la fecha de recepcion
```

### `PositionType.java`

Clasifica una ubicacion `0x31`:

```text
REAL_TIME -> enviada en tiempo real
RE_UPLOAD -> reenviada por el GPS
UNKNOWN   -> no se pudo determinar
```

---

## 6. Capa de servicios

### `GpsDeviceService.java`

Administra los dispositivos en memoria.

Usa un `ConcurrentHashMap<String, GpsDevice>`.

Responsabilidades:

- Registrar un IMEI nuevo.
- Marcar un dispositivo como conectado.
- Actualizar `lastConnection`.
- Consultar un dispositivo.
- Verificar si existe.

Metodo importante:

```java
registerDevice(String imei, Socket socket)
```

Actualmente el socket se recibe para mantener compatibilidad con el flujo TCP, pero el servicio solo usa el IMEI y actualiza el estado del dispositivo.

### `GpsLocationService.java`

Administra las ubicaciones en memoria.

La estructura interna es conceptualmente:

```text
IMEI -> lista de GpsLocation
```

Responsabilidades:

- Guardar ubicaciones por IMEI.
- Obtener la ultima ubicacion.
- Priorizar la ultima ubicacion `REAL_TIME` cuando se solicita la ubicacion actual.
- Devolver el historial completo.

Nota: el mapa es concurrente, pero las listas internas deben revisarse antes de usar el sistema con muchos dispositivos simultaneos. Para produccion podria usarse una coleccion concurrente o persistencia en base de datos.

---

## 7. Parsers

Los parsers reciben bytes y los convierten en modelos Java.

### `GpsPacketParser.java`

Es la fachada publica del parseo de paquetes generales del protocolo `78 78`. Mantiene los metodos que usan los handlers y las pruebas, pero delega el trabajo en clases especializadas:

- `GpsLocationDecoder` para ubicaciones `0x31`.
- `GpsAlarmDecoder` para alarmas `0x32`.
- `GpsDateParser` para fechas.
- `GpsTimestampService` para comparar timestamps.

Procesa especialmente:

- Ubicacion `0x31`.
- Alarma `0x32`.
- Fechas GPS.
- Estado temporal del paquete.
- Tipo de posicion.

### `GpsDateParser.java`

Convierte los seis bytes de fecha del protocolo en un `OffsetDateTime` UTC. El formato es `yy mm dd hh mi ss`.

### `GpsTimestampService.java`

Compara la fecha enviada por el GPS con la fecha de recepcion del servidor y devuelve `VALID`, `STALE` o `INVALID`. Tambien formatea fechas para la zona horaria de Colombia.

### `GpsLocationDecoder.java`

Decodifica exclusivamente la ubicacion `0x31`:

Extrae:

- Fecha y hora desde `data[4..9]`.
- Satelites desde `data[10]`.
- Latitud desde `data[11..14]`.
- Longitud desde `data[15..18]`.
- Velocidad desde `data[19]`.
- Curso desde `data[20..21]`.

La fecha del dispositivo se interpreta en UTC. Luego se guarda tambien el instante de recepcion del servidor para comparar el desfase.

Ademas crea el objeto `GpsLocation` y solicita al printer la salida diagnostica.

### `GpsAlarmDecoder.java`

Decodifica exclusivamente la alarma `0x32`. Usa `GpsDateParser`, `GpsTimestampService` y `GpsPacketDebugPrinter`.

### `GpsPositionTypeDetector.java`

Determina si una ubicacion `0x31` es `REAL_TIME`, `RE_UPLOAD` o `UNKNOWN`, revisando la extension de upload y la longitud de la trama.

### `GpsPacketDebugPrinter.java`

Contiene la salida diagnostica de ubicaciones y alarmas: fechas, coordenadas, bytes hexadecimales y estados. Mantener esta responsabilidad separada evita que los decoders mezclen interpretacion de datos con impresion en consola.

### `GpsLbsParser.java`

Procesa el protocolo LBS `0x50`.

Extrae:

- Fecha GPS.
- TA.
- MCC.
- MNC.
- LAC.
- Cell ID.
- Intensidad de senal.
- Estado temporal.

Devuelve un objeto `GpsLbsData`.

El parser conserva la fecha del paquete tal como la reporta el dispositivo. La conversion a hora de Colombia se usa para mostrar informacion, no para cambiar el valor almacenado.

---

## 8. Servidor TCP

### `GpsTcpServer.java`

Es el punto de entrada de la comunicacion TCP.

Responsabilidades:

- Abrir un `ServerSocket` en el puerto `8842`.
- Esperar conexiones entrantes.
- Crear un hilo para cada cliente GPS.
- Delegar el socket a `GpsClientHandler`.
- Exponer operaciones compatibles para consultar conexiones y enviar comandos.

No debe conocer el detalle de cada protocolo GPS. Esa logica esta en `tcp.protocol`.

Flujo:

```text
GpsTcpServer
    |
    | acepta Socket
    v
GpsClientHandler
    |
    | lee bytes
    v
GpsFrameDecoder
    |
    | devuelve tramas completas
    v
GpsPacketRouter
    |
    | identifica protocolo
    v
Handler especifico
```

---

## 9. Manejo de conexiones

### `GpsClientHandler.java`

Administra una conexion individual.

Responsabilidades:

- Leer bytes desde `InputStream`.
- Crear un `GpsFrameDecoder` propio para esa conexion.
- Procesar cada trama completa.
- Mantener el IMEI asociado al socket.
- Registrar y retirar la conexion.
- Cerrar el socket cuando termina la comunicacion.

Es importante que el decoder sea independiente por cliente. Si se compartiera entre GPS, los bytes fragmentados de un dispositivo podrian mezclarse con los de otro.

### `GpsConnectionRegistry.java`

Mantiene el mapa de conexiones activas:

```text
IMEI -> Socket
```

Responsabilidades:

- Registrar un socket para un IMEI.
- Eliminar un socket al desconectarse.
- Verificar si esta conectado.
- Enviar comandos a un GPS conectado.

Usa `ConcurrentHashMap` para permitir acceso desde varios hilos.

---

## 10. Protocolo TCP

### `GpsFrameDecoder.java`

Resuelve una caracteristica importante de TCP: un `read()` no necesariamente equivale a un paquete GPS.

Puede ocurrir que:

```text
Un paquete llegue dividido en varios read()
Varios paquetes lleguen en un solo read()
Lleguen bytes basura antes de un paquete
```

El decoder:

1. Acumula bytes pendientes.
2. Busca headers `78 78` o `79 79`.
3. Lee la longitud declarada como minimo esperado.
4. Busca el footer `0D 0A`.
5. Devuelve solo tramas completas.
6. Conserva bytes incompletos para la siguiente lectura.
7. Descarta datos excesivos para evitar crecimiento indefinido.

### `GpsPacketHandler.java`

Es la interfaz comun para los procesadores de paquetes.

Todos los handlers reciben:

- `Socket`.
- Bytes del paquete.
- Longitud.
- IMEI actual.

Y devuelven el IMEI que debe conservar la conexion.

Esta interfaz permite que el router trate todos los protocolos de la misma forma.

### `GpsPacketRouter.java`

Decide que handler debe procesar una trama.

Enrutamiento actual:

| Header/protocolo | Handler |
|---|---|
| `78 78`, `0x01` | `LoginPacketHandler` |
| `78 78`, `0x13` | `HeartbeatPacketHandler` |
| `78 78`, `0x31` | `LocationPacketHandler` |
| `78 78`, `0x32` | `AlarmPacketHandler` |
| `78 78`, `0x50` | `LbsPacketHandler` |
| `78 78`, `0x80` | `CommandPacketHandler` |
| `79 79` | `LegacyPacketHandler` |
| Cualquier otro | `UnknownPacketHandler` |

El router no interpreta todos los campos. Solo identifica el destino correcto.

---

## 11. ACK y handlers

### `GpsAckService.java`

Centraliza la respuesta que se envia al GPS.

Responsabilidades:

- Construir el ACK.
- Copiar el protocolo recibido.
- Extraer el numero de secuencia.
- Calcular CRC-16 tipo `0x1021`.
- Agregar footer `0D 0A`.
- Escribir la respuesta en el socket.

La estructura general del ACK es:

```text
78 78 | longitud | protocolo | serial | CRC | 0D 0A
```

### `LoginPacketHandler.java`

Procesa `0x01`.

- Extrae el IMEI.
- Registra el dispositivo.
- Asocia el IMEI con la conexion.
- Envia ACK.
- Devuelve el IMEI detectado.

### `HeartbeatPacketHandler.java`

Procesa `0x13`.

- Registra que se recibio heartbeat.
- Envia ACK.
- Conserva el IMEI actual.

### `LocationPacketHandler.java`

Procesa `0x31`.

- Verifica que exista un IMEI asociado.
- Usa `GpsPacketParser`.
- Guarda el `GpsLocation` mediante `GpsLocationService`.
- Envia ACK.

### `AlarmPacketHandler.java`

Procesa `0x32`.

- Usa `GpsPacketParser.parseAlarm`.
- Conserva el IMEI actual.
- Envia ACK.

### `LbsPacketHandler.java`

Procesa `0x50`.

- Usa `GpsLbsParser`.
- Conserva los datos recibidos en el parser.
- Envia ACK.

### `CommandPacketHandler.java`

Procesa `0x80`.

- Registra que se recibio un comando.
- Envia ACK.
- Conserva el IMEI actual.

### `LegacyPacketHandler.java`

Procesa paquetes con header `79 79`.

- Lee la longitud declarada.
- Lee el protocolo interno.
- Reconoce informacion basica del protocolo `0x94`.
- Conserva el IMEI.

Es una base para ampliar el soporte de paquetes antiguos o alternativos.

### `UnknownPacketHandler.java`

Maneja headers o protocolos que aun no estan implementados.

- Muestra los primeros bytes para diagnostico.
- No guarda datos.
- No cambia el IMEI de la conexion.

---

## 12. Flujo completo de una ubicacion

```text
1. El GPS abre una conexion TCP.
2. GpsTcpServer acepta el Socket.
3. GpsClientHandler comienza a leer bytes.
4. GpsFrameDecoder reconstruye la trama completa.
5. GpsPacketRouter lee el header y el protocolo.
6. Se selecciona LocationPacketHandler para 0x31.
7. LocationPacketHandler verifica el IMEI.
8. GpsPacketParser decodifica fecha, coordenadas, velocidad y curso.
9. Se compara la fecha GPS con la recepcion del servidor.
10. GpsLocationService guarda la ubicacion.
11. GpsAckService construye y envia el ACK.
12. El GPS continua conectado para enviar el siguiente paquete.
```

---

## 13. Flujo de login

```text
GPS envia 0x01
        |
        v
GpsFrameDecoder
        |
        v
GpsPacketRouter
        |
        v
LoginPacketHandler
        |
        +--> extrae IMEI
        +--> GpsDeviceService registra dispositivo
        +--> GpsAckService envia ACK
        +--> GpsClientHandler guarda currentImei
        +--> GpsConnectionRegistry guarda IMEI -> Socket
```

El login es importante porque los paquetes posteriores normalmente necesitan conocer el IMEI asociado al socket.

---

## 14. Pruebas

### `GpsBackendApplicationTests.java`

Prueba:

- Carga del contexto Spring.
- Conversion de fechas GPS.
- Parseo de ubicaciones.
- Parseo de alarmas.
- Parseo LBS.
- Construccion de ACK.
- Numero de secuencia de ACK.
- Preservacion de `receivedAt`.

Algunas pruebas actuales invocan `buildAck` mediante reflexion para conservar compatibilidad con la clase `GpsTcpServer`.

### `GpsFrameDecoderTests.java`

Prueba los casos propios de TCP:

- Una trama dividida entre varias lecturas.
- Dos tramas concatenadas en una lectura.

Estas pruebas protegen la parte mas importante del decoder: no enviar un paquete incompleto al router y no perder paquetes cuando llegan agrupados.

---

## 15. Configuracion y ejecucion

### `application.properties`

Configuracion actual:

```properties
spring.application.name=gps-backend
server.port=8080
```

El servidor HTTP usa el puerto `8080`.

El servidor TCP GPS usa actualmente el puerto `8842`, definido en `GpsTcpServer`.

Para ejecutar las pruebas:

```powershell
.\mvnw.cmd test
```

Para empaquetar sin ejecutar pruebas:

```powershell
.\mvnw.cmd package -DskipTests
```

---

## 16. Decisiones de arquitectura

### Por que no dejar todo en `GpsTcpServer`

Un servidor monolitico terminaria mezclando:

- Sockets.
- Threads.
- Lectura de bytes.
- Reconstruccion de tramas.
- Enrutamiento.
- Parseo.
- ACK y CRC.
- Persistencia.
- Logs.

Separar estas responsabilidades permite probar y modificar cada parte sin tocar todo el flujo.

### Beneficios de la estructura actual

- `GpsTcpServer` es pequeno y facil de entender.
- Un nuevo protocolo puede agregarse como un handler separado.
- El ACK se prueba de forma independiente.
- La lectura TCP no esta mezclada con el parseo GPS.
- Las conexiones estan aisladas de los datos de ubicacion.
- Los controllers REST no conocen detalles del protocolo TCP.

---

## 17. Mejoras futuras recomendadas

Estas mejoras no son necesarias para entender la estructura actual, pero serian convenientes antes de produccion:

1. Mover el puerto `8842` a `application.properties`.
2. Reemplazar `System.out` y `System.err` por logging de Spring.
3. Usar un `ExecutorService` o `ThreadPoolTaskExecutor` en lugar de crear hilos sin limite.
4. Usar una coleccion concurrente para las listas internas de ubicaciones.
5. Persistir dispositivos y ubicaciones en SQL Server.
6. Agregar validacion de CRC de los paquetes recibidos.
7. Completar el soporte del protocolo `79 79`.
8. Separar los logs de diagnostico del parseo de datos.
9. Eliminar gradualmente la compatibilidad basada en reflexion de las pruebas y probar `GpsAckService` directamente.
10. Agregar pruebas unitarias para cada handler y para el router.

---

## 18. Resumen

La arquitectura actual sigue este principio:

```text
Servidor TCP
    -> Conexion del cliente
        -> Decoder de tramas
            -> Router de protocolos
                -> Handler especifico
                    -> Parser
                        -> Service
                            -> Modelo
                    -> ACK
```

Cada capa tiene una responsabilidad concreta y puede evolucionar sin convertir nuevamente `GpsTcpServer` en una clase monolitica.
