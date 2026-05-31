# IP Location Service

Spring Boot microservice for resolving public IP addresses using the MaxMind GeoLite2 offline database. The deployed cloud build focuses on IP lookup, Redis caching, MySQL audit logging, and a lightweight web client.

## Features

- IP geolocation through MaxMind GeoLite2 City data
- Redis look-aside cache with 24-hour TTL for repeated IP lookups
- MySQL API usage logging with request type, input, response status, client IP, and processing time
- Bean Validation and global JSON error handling
- Swagger/OpenAPI documentation
- Docker Compose setup for local MySQL and Redis
- Render/Aiven-ready configuration through environment variables

## Cloud Deployment Note

The cloud deployment supports IP lookup only. GPS reverse lookup is disabled in the Render profile because the Who's On First SQLite dataset is too large for the lightweight deployment target.

## API

### IP Lookup

```http
POST /api/location/ip
Content-Type: application/json

{
  "ip": "8.8.8.8"
}
```

Example response:

```json
{
  "ipAddress": "8.8.8.8",
  "city": "Mountain View",
  "state": "California",
  "country": "United States",
  "latitude": 37.386,
  "longitude": -122.0838,
  "message": "Source: MaxMind Database"
}
```

## Local Setup

```bash
docker compose up --build
```

The app runs on `http://localhost:8080`. The frontend client is available at `http://localhost:8080/userform.html`.

## Required Environment Variables

```env
SPRING_DATASOURCE_URL=jdbc:mysql://host:3306/location_service
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=
SPRING_REDIS_USERNAME=default
SPRING_REDIS_SSL=false
```

## Testing

```bash
./mvnw test
```

Tests use an in-memory H2 database profile so they do not require a live MySQL instance.
