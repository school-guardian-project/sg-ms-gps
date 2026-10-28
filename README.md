# Guardian Escolar — GPS Service

## Overview

The GPS Service is a microservice of the Guardian Escolar platform responsible for receiving tracking data from GPS devices installed in school transportation vehicles and providing location information through an HTTP REST API.

It uses TCP communication to receive GPS device messages and HTTP to expose its API to other components of the system.

## Main Responsibilities

- Receive GPS tracking messages through a TCP listener.
- Identify tracking devices using their IMEI.
- Process incoming GPS information.
- Keep the latest received location in application memory.
- Expose location information through the REST API.

## Technology Stack

| Technology | Description |
|---|---|
| Java 21 | Programming language |
| Spring Boot 4.1.1 | Application framework |
| Maven | Build and dependency management |
| Spring Web MVC | HTTP API |
| TCP | Communication with GPS devices |
| Docker | Application containerization |

The project also declares JPA and Microsoft SQL Server JDBC dependencies. However, the current application configuration disables database and JPA auto-configuration. GPS location persistence is therefore not enabled by this configuration.

## Communication and Ports

| Protocol | Port | Purpose |
|---|---:|---|
| HTTP | `8080` | REST API |
| TCP | `8842` | Incoming GPS device messages |

The HTTP API runs on container port `8080`. Docker maps it to `127.0.0.1:8080` on the host for local debugging.

The TCP listener uses port `8842`, published through Docker as `8842:8842` to allow incoming connections from GPS devices, subject to network and firewall configuration.

### API Gateway

The service connects to the shared Docker network `sg-services-network`. When Kong is connected to this network, it can reach the service at:

`http://ms-gps:8080`

The inspected primary gateway exposes `/api/gps` without stripping that prefix.
It requires JWT authentication. This does not establish which gateway revision is
deployed; the separate gateway checkout has its own revision.

### Verified HTTP interface

| Method | Path | Successful response |
|---|---|---|
| GET | `/api/gps/devices/{imei}` | Device with `imei`, `gpsStatus`, `lastConnection`; empty body if unknown |
| GET | `/api/gps/devices/{imei}/connected` | Boolean; `false` if unknown |
| GET | `/api/gps/devices/{imei}/location` | Latest `GpsLocation`; empty body if unknown |
| POST | `/api/gps/test/{imei}` | Stores and returns a fixed synthetic test location |

These are the routes of `GpsDeviceController`, `GpsLocationController` and
`GpsTestController`. A null return is not an implemented 404 response. There is
no REST health, registration, status-update or location-history controller.
The test endpoint is not development-gated in the current implementation; do
not invoke it against real devices to check availability.

For a read-only request through the local gateway (replace the IMEI and use
your own access token):

```bash
curl -H "Authorization: Bearer $ACCESS_TOKEN" \
  http://localhost:8000/api/gps/devices/TEST_IMEI/location
```

`GpsLocation` contains `imei`, numeric `latitude`, `longitude`, `speed`, `course`,
and nullable time/status fields: `dateTime`, `gpsDateTime`, `receivedAt`,
`timestampStatus`, `status`, `positionType`. Timestamp status is `VALID`, `STALE`
or `INVALID`; position type is `UNKNOWN`, `REAL_TIME` or `RE_UPLOAD`.
The exact optional values depend on the received message.

The mobile `GpsService` requests this location path using a URL-encoded IMEI.
The web bus form instead calls `/fleet/api/gps-devices`, which is fleet's
database inventory API: its UUID identifies the inventory record, not a GPS
tracking request. Do not substitute that UUID for the tracking IMEI.

Source comparison was static. Java controller execution, native-device behavior,
and deployed Kong forwarding still require runtime verification.

## GPS Data Handling

The service receives device messages through TCP and uses the device IMEI to identify the tracking device.

The latest location is retained in application memory. It is not currently persisted to SQL Server, and the service does not provide a persisted location history.

As a result, restarting the service clears the in-memory location state. The latest location becomes available again after the service receives and processes new GPS data.

## Database Decision

The project documents this approach in **ADR-010 — GPS Location Persistence**.

The current decision is not to implement GPS location persistence because the required functionality focuses on displaying the latest vehicle location rather than consulting historical routes.

The existing `Gps.GpsDevice` and `Gps.GpsLocation` database tables remain part of the database schema. Their existence does not mean that the GPS Service currently writes location data to them.

## Running with Docker Compose

### Prerequisites

- Docker and Docker Compose.
- The `sg-services-network` Docker network.
- Network access for GPS devices to reach TCP port `8842`.

If the shared Docker network does not already exist, create it:

```bash
docker network create sg-services-network
```

Do not run this command if the network already exists.

### Start the service

From the directory containing `docker-compose.yml`, run:

```bash
docker compose up --build -d
```

### Check the container

```bash
docker compose ps
```

### View application logs

```bash
docker compose logs -f ms-gps
```

### Stop the service

```bash
docker compose down
```

## Configuration

The current `application.properties` defines:

- Application name: `gps-backend`.
- HTTP server port: `8080`.
- Exclusions for database and JPA auto-configuration.

The TCP listener configuration and supported GPS message format must be verified against the Java implementation.

## Current Limitations

- GPS locations are stored in memory rather than persisted in SQL Server.
- Location data is lost when the service restarts.
- Historical GPS tracking is not persisted.
- The HTTP surface above is source-verified, not a deployed end-to-end guarantee.
- GPS device connectivity depends on the correct server address, TCP port and network configuration.
- The presence of database dependencies does not imply active database persistence.


