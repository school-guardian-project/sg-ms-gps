# Servicio GPS

GPS tracking microservice for School Guardian. Receives tracking data from GPS devices installed in school transportation vehicles and provides location information through an HTTP REST API. Built with Java 21 and Maven.

## Getting Started

These instructions will give you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on deploying the project on a live system.

### Prerequisites

Requirements for the software and other tools to build, test and push:

- Java 21
- Maven 3.9
- Docker
- Docker Compose
- The `sg-services-network` Docker network (external, shared across services)

### Installing

A step by step series of examples that tell you how to get a development environment running.

Create the external network if it does not exist:

    docker network create sg-services-network

Start the service:

    docker compose up -d

The HTTP API listens on port 8080 (mapped to `127.0.0.1:8080` for local debugging). The GPS device server listens on TCP port 8842, published on every interface so devices can reach it. Kong terminates TLS and reaches the service as `http://ms-gps:8080` over the Docker network.

## Running the tests

Explain how to run the automated tests for this system.

### Sample Tests

Tests are executed against the test service that runs in Docker Compose for each service.

### Style test

Checks if the best practices and the right coding style has been used, validated through the test service in Docker Compose.

## Deployment

Deployment is performed via GitHub Actions to an AWS EC2 instance.

## Built With

- Java 21
- Maven 3.9
- Docker Compose — Local development stack

## Contributing

Please read [CONTRIBUTING.md](https://github.com/school-guardian-project/.github/blob/main/CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the SCHOOL GUARDIAN PROPRIETARY SOFTWARE LICENSE Version 1.0 — 2026. See the [LICENSE](LICENSE) file for details.
