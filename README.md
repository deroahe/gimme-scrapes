# Gimme Scrapes

Romanian Real Estate Scraper Application

## Overview

Gimme Scrapes is a comprehensive web scraping and aggregation platform for Romanian real estate listings. It automatically collects, processes, and presents property listings from major Romanian real estate websites including imobiliare.ro, olx.ro, and storia.ro.

## Architecture

This is a multi-service application built with:

- **Backend**: Java 21 + Spring Boot 3.x
- **Database**: PostgreSQL 16
- **Cache**: Redis 7
- **Message Queue**: RabbitMQ 3.13
- **Frontend**: React 18 + TypeScript
- **Deployment**: Docker Compose

### Services

1. **gs-commons** - Shared DTOs, utilities, and domain models
2. **gs-orchestrator** - Schedules scraping jobs and provides admin REST API (port 8080)
3. **gs-worker** - Consumes jobs from RabbitMQ, scrapes websites, sends emails (port 8081)
4. **gs-ui** - React frontend for browsing listings (port 3000)

## Prerequisites

- **Java 21** - [Download](https://adoptium.net/)
- **Maven 3.9+** - [Download](https://maven.apache.org/download.cgi)
- **Node.js 20+** - [Download](https://nodejs.org/)
- **Docker & Docker Compose** - [Download](https://www.docker.com/products/docker-desktop)

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd gimme-scrapes
```

### 2. Start Infrastructure Services

Start PostgreSQL, Redis, and RabbitMQ:

```bash
docker-compose up -d postgres redis rabbitmq
```

Verify services are running:
- PostgreSQL: `localhost:5432`
- Redis: `localhost:6379`
- RabbitMQ Management UI: http://localhost:15672 (admin/admin)

### 3. Build the Backend

```bash
# Build all modules
mvn clean install

# Or build and skip tests
mvn clean install -DskipTests
```

### 4. Run Backend Services

**Terminal 1 - Orchestrator:**
```bash
mvn spring-boot:run -pl gs-orchestrator
```

**Terminal 2 - Worker:**
```bash
mvn spring-boot:run -pl gs-worker
```

### 5. Run Frontend

```bash
cd gs-ui
npm install
npm start
```

The frontend will be available at http://localhost:3000

## Environment Configuration

Copy the example environment file and customize it:

```bash
cp .env.example .env
```

Edit `.env` with your configuration:

```env
# Database
POSTGRES_HOST=localhost
POSTGRES_DB=gimme_scrapes
POSTGRES_USER=gimmescrapes
POSTGRES_PASSWORD=password

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=admin

# Email (for notifications)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

## API Endpoints

### Orchestrator Service (port 8080)

**Public API:**
- `GET /api/listings` - List all listings with filters
- `GET /api/listings/{id}` - Get listing details
- `GET /api/listings/filters` - Get available filter options
- `GET /api/sources` - List all scraping sources

**Admin API:**
- `POST /api/admin/scrape/trigger` - Trigger scraping for all sources
- `POST /api/admin/scrape/trigger/{sourceName}` - Trigger specific source
- `GET /api/admin/scrape/status` - Get current scraping status
- `GET /api/admin/scrape/history` - Get job history
- `GET /api/admin/sources` - Manage sources
- `POST /api/admin/cache/invalidate` - Clear cache

**Actuator:**
- `GET /actuator/health` - Health check
- `GET /actuator/metrics` - Metrics
- `GET /actuator/prometheus` - Prometheus metrics

## Development

### Project Structure

```
gimme-scrapes/
├── pom.xml                    # Parent POM
├── docker-compose.yml         # Infrastructure services
├── .env.example              # Environment template
├── gs-commons/               # Shared library
│   ├── src/main/java/.../commons/
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── model/           # JPA Entities
│   │   ├── enums/           # Enumerations
│   │   └── util/            # Utilities
├── gs-orchestrator/          # Orchestrator service
│   ├── src/main/java/.../orchestrator/
│   │   ├── config/          # Configuration
│   │   ├── controller/      # REST Controllers
│   │   ├── scheduler/       # Scheduled jobs
│   │   └── service/         # Business logic
│   └── src/main/resources/
│       └── application.yml  # Configuration
├── gs-worker/                # Worker service
│   ├── src/main/java/.../worker/
│   │   ├── config/          # Configuration
│   │   ├── consumer/        # RabbitMQ listeners
│   │   ├── scraper/         # Scraper implementations
│   │   ├── service/         # Business logic
│   │   └── cache/           # Cache services
│   └── src/main/resources/
│       └── application.yml  # Configuration
└── gs-ui/                    # React frontend
    ├── public/
    ├── src/
    │   ├── components/      # React components
    │   ├── pages/           # Page components
    │   ├── services/        # API services
    │   └── types/           # TypeScript types
    └── package.json
```

### Running Tests

```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl gs-orchestrator
```

### Building for Production

```bash
# Build backend
mvn clean package -DskipTests

# Build frontend
cd gs-ui
npm run build
```

## Docker Deployment

Build and run all services with Docker:

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Monitoring

RabbitMQ Management Console: http://localhost:15672
- Username: `admin`
- Password: `admin`

Health checks:
- Orchestrator: http://localhost:8080/actuator/health
- Worker: http://localhost:8081/actuator/health

## Troubleshooting

**Database connection issues:**
- Verify PostgreSQL is running: `docker-compose ps postgres`
- Check logs: `docker-compose logs postgres`

**RabbitMQ connection issues:**
- Check RabbitMQ is healthy: http://localhost:15672
- Verify credentials in `.env` match `docker-compose.yml`

**Port conflicts:**
- Ensure ports 5432, 6379, 5672, 15672, 8080, 8081, 3000 are available

## Features (Planned)

- ✅ Multi-module Maven project
- ✅ Docker Compose infrastructure
- ✅ Spring Boot microservices
- ✅ React + TypeScript UI
- ⬜ Web scraping for Romanian real estate sites
- ⬜ Redis caching layer
- ⬜ RabbitMQ job queue
- ⬜ Email notifications
- ⬜ Admin dashboard
- ⬜ Prometheus metrics
- ⬜ Grafana dashboards

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]