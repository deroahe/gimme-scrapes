# Gimme Scrapes - Implementation Plan

Romanian Real Estate Scraper Application

## Technology Stack

### Core Technologies
- **Java 21** with Spring Boot 3.x
- **PostgreSQL** - relational database
- **Redis** - caching layer
- **RabbitMQ** - message broker for job queuing
- **React 18** with TypeScript for frontend
- **Docker Compose** - local deployment

### Spring Boot Modules
- Spring Boot Web (MVC)
- Spring Data JPA (PostgreSQL integration)
- Spring Data Redis
- Spring AMQP (RabbitMQ)
- Spring Mail (async email sending)
- Spring Scheduler (job scheduling in orchestrator)

### Scraping
- **Jsoup** - HTML parsing for Romanian real estate sites

### Frontend Stack
- React 18 with TypeScript
- Material-UI (MUI)
- React Query (TanStack Query)
- Axios

### Additional Tools
- Lombok - reduce boilerplate
- MapStruct - type-safe bean mapping
- Flyway - database migrations
- Spring Actuator - monitoring/health checks
- SLF4J/Logback - structured logging
- Handlebars - email templates

## Architecture

### 3-Service Architecture
1. **gs-orchestrator** - Schedules jobs, admin API
2. **gs-worker** - Consumes jobs, scrapes, caches
3. **gs-ui** - React frontend
4. **gs-commons** - Shared DTOs, utilities, models

---

## Project Structure

```
gimme-scrapes/
├── pom.xml (parent)
├── docker-compose.yml
├── docker-compose.monitoring.yml
├── implementation-plan.md
├── README.md
├── gs-orchestrator/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/gimmescrapes/orchestrator/
│           │   ├── OrchestratorApplication.java
│           │   ├── config/
│           │   ├── controller/
│           │   ├── scheduler/
│           │   └── service/
│           └── resources/
│               └── application.yml
├── gs-worker/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/gimmescrapes/worker/
│           │   ├── WorkerApplication.java
│           │   ├── config/
│           │   ├── consumer/
│           │   ├── scraper/
│           │   │   ├── RealEstateScraper.java
│           │   │   ├── ImobiliareScraper.java
│           │   │   ├── OlxScraper.java
│           │   │   └── StoriaRoScraper.java
│           │   ├── service/
│           │   └── cache/
│           └── resources/
│               └── application.yml
├── gs-ui/
│   ├── package.json
│   ├── tsconfig.json
│   ├── public/
│   └── src/
│       ├── components/
│       ├── pages/
│       ├── services/
│       ├── types/
│       └── App.tsx
├── gs-commons/
│   ├── pom.xml
│   └── src/
│       └── main/
│           └── java/com/gimmescrapes/commons/
│               ├── dto/
│               ├── model/
│               ├── enums/
│               └── util/
└── .gitignore
```

---

## Phase 1: Project Setup & Infrastructure

### Tasks
1. Create Maven multi-module project structure
2. Configure parent POM with dependency management
3. Set up basic Spring Boot applications for gs-orchestrator and gs-worker
4. Create gs-commons library module
5. Set up gs-ui with Create React App + TypeScript
6. Create docker-compose.yml with:
   - PostgreSQL 16
   - Redis 7
   - RabbitMQ 3.13 with management plugin
7. Create .gitignore for Java, Node.js, IDE files
8. Initialize Git repository

### Deliverables
- Working multi-module Maven project
- All services can start (even if empty)
- Docker Compose starts all infrastructure
- README with setup instructions

---

## Phase 2: Domain Model & Database Schema

### Database Schema

#### Entities

**listings**
- `id` (BIGSERIAL PRIMARY KEY)
- `source_id` (FK to sources)
- `external_id` (VARCHAR, unique per source)
- `url` (VARCHAR UNIQUE NOT NULL)
- `title` (VARCHAR)
- `description` (TEXT)
- `price` (DECIMAL)
- `currency` (VARCHAR, default 'EUR')
- `surface_sqm` (DECIMAL)
- `rooms` (INTEGER)
- `bathrooms` (INTEGER)
- `floor` (INTEGER)
- `total_floors` (INTEGER)
- `year_built` (INTEGER)
- `city` (VARCHAR)
- `neighborhood` (VARCHAR)
- `address` (VARCHAR)
- `latitude` (DECIMAL)
- `longitude` (DECIMAL)
- `image_urls` (TEXT[])
- `features` (JSONB) - parking, balcony, etc.
- `first_scraped_at` (TIMESTAMP)
- `last_scraped_at` (TIMESTAMP)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**sources**
- `id` (BIGSERIAL PRIMARY KEY)
- `name` (VARCHAR UNIQUE) - 'imobiliare.ro', 'olx.ro', etc.
- `display_name` (VARCHAR)
- `base_url` (VARCHAR)
- `enabled` (BOOLEAN)
- `scrape_interval_minutes` (INTEGER)
- `last_scrape_at` (TIMESTAMP)
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**scraping_jobs**
- `id` (BIGSERIAL PRIMARY KEY)
- `source_id` (FK to sources)
- `status` (VARCHAR) - RUNNING, COMPLETED, FAILED
- `started_at` (TIMESTAMP)
- `completed_at` (TIMESTAMP)
- `items_scraped` (INTEGER)
- `items_new` (INTEGER)
- `items_updated` (INTEGER)
- `error_message` (TEXT)
- `created_at` (TIMESTAMP)

**email_subscriptions**
- `id` (BIGSERIAL PRIMARY KEY)
- `email` (VARCHAR UNIQUE NOT NULL)
- `active` (BOOLEAN)
- `preferences` (JSONB) - filters: city, priceMin, priceMax, rooms, etc.
- `created_at` (TIMESTAMP)
- `updated_at` (TIMESTAMP)

**email_jobs**
- `id` (BIGSERIAL PRIMARY KEY)
- `subscription_id` (FK to email_subscriptions, nullable)
- `recipient_email` (VARCHAR)
- `subject` (VARCHAR)
- `status` (VARCHAR) - PENDING, SENT, FAILED
- `sent_at` (TIMESTAMP)
- `error_message` (TEXT)
- `created_at` (TIMESTAMP)

#### Indexes
- `idx_listings_city` on listings(city)
- `idx_listings_price` on listings(price)
- `idx_listings_scraped` on listings(last_scraped_at)
- `idx_listings_source` on listings(source_id)
- `idx_listings_url` on listings(url)
- `idx_scraping_jobs_source` on scraping_jobs(source_id)
- `idx_scraping_jobs_status` on scraping_jobs(status)

### Tasks
1. Create JPA entities in gs-commons
2. Create Flyway migrations (V1__initial_schema.sql)
3. Create seed data migration (V2__seed_sources.sql) for Romanian real estate sources
4. Configure Spring Data JPA repositories
5. Set up database connection in both services

### Deliverables
- Complete domain model in gs-commons
- Database schema created via Flyway
- Repository interfaces defined

---

## Phase 3: RabbitMQ Configuration & Messaging

### Queue Architecture

**Exchanges & Queues:**
- `scrape.exchange` (direct)
  - `scrape.queue` → DLQ: `scrape.dlq`
- `email.exchange` (direct)
  - `email.queue` → DLQ: `email.dlq`

**Retry Strategy:**
- Max retries: 3
- TTL-based exponential backoff: 1m, 5m, 15m
- Failed jobs after max retries → DLQ

### Message Models (in gs-commons)

**ScrapeJobMessage:**
```json
{
  "jobId": "uuid",
  "sourceId": 1,
  "sourceName": "imobiliare.ro",
  "triggeredBy": "SCHEDULED|MANUAL",
  "timestamp": "2025-01-15T10:00:00Z"
}
```

**EmailJobMessage:**
```json
{
  "jobId": "uuid",
  "recipientEmail": "user@example.com",
  "emailType": "TOP_LISTINGS|ALERT",
  "data": {
    "listings": [...],
    "filters": {...}
  },
  "timestamp": "2025-01-15T09:00:00Z"
}
```

### Tasks
1. Configure RabbitMQ in application.yml for both services
2. Create @Configuration classes for exchanges, queues, bindings
3. Implement retry and DLQ logic
4. Create message publisher services in gs-orchestrator
5. Create message consumer listeners in gs-worker
6. Add error handling and logging

### Deliverables
- RabbitMQ fully configured with retry logic
- Message publishers in gs-orchestrator
- Message consumers in gs-worker
- DLQ monitoring capability

---

## Phase 4: Worker Service - Scraping Implementation

### Scraper Strategy Pattern

**Interface:**
```java
public interface RealEstateScraper {
    List<ListingDto> scrape(Source source) throws ScrapingException;
    boolean supports(String sourceName);
    String getSourceName();
}
```

**Implementations (Romanian Real Estate Sites):**
1. **ImobiliareScraper** - imobiliare.ro
2. **OlxScraper** - olx.ro
3. **StoriaRoScraper** - storia.ro (rebranded from OLX Imobiliare)

### Scraping Features
- User-Agent rotation
- Request throttling (delay between requests)
- HTML parsing with Jsoup
- Data normalization (price, surface, rooms)
- Image URL extraction
- Duplicate detection by URL
- Price change tracking
- Bulk upsert to database

### Error Handling
- Per-listing error handling (don't fail entire job for 1 bad listing)
- Structured logging (source, listing URL, error type)
- Job status tracking in scraping_jobs table
- Retry via RabbitMQ for transient failures

### Tasks
1. Create RealEstateScraper interface in gs-commons
2. Implement ImobiliareScraper in gs-worker
3. Implement OlxScraper in gs-worker
4. Implement StoriaRoScraper in gs-worker
5. Create ScraperService with strategy selection
6. Implement duplicate detection logic
7. Implement bulk upsert repository method
8. Create @RabbitListener for scrape.queue
9. Integrate scraping with job status tracking
10. Add comprehensive logging and error handling

### Deliverables
- Working scrapers for 3 Romanian real estate sites
- Listings saved to database with deduplication
- Job execution tracked in scraping_jobs table
- Robust error handling and retry logic

---

## Phase 5: Redis Caching Layer

### Cache Strategy

**Cache Keys:**
- `listings:all:{page}:{size}:{filters-hash}` - paginated results
- `listings:filters:metadata` - available filters (cities, price range)
- `listings:count:{filters-hash}` - total count for pagination

**Configuration:**
- Default TTL: 30 minutes
- Max memory policy: `allkeys-lru`
- Eviction when memory > 80%

**Cache-Aside Pattern:**
1. Check Redis cache
2. If miss → query PostgreSQL
3. Store result in Redis with TTL
4. Return result

**Cache Invalidation:**
- After each successful scrape job → warm cache for popular filters
- Daily cleanup job → remove stale entries
- Manual invalidation via admin API

### Tasks
1. Configure Redis in application.yml
2. Create RedisConfig with Jackson serialization
3. Implement CacheService for cache operations
4. Create cache key generator utility
5. Implement cache warming after scraping
6. Create scheduled cache cleanup job
7. Add cache hit/miss metrics
8. Implement cache invalidation in admin API

### Deliverables
- Redis caching fully integrated
- Cache warming after scraping
- Periodic cleanup job
- Cache metrics exposed

---

## Phase 6: Orchestrator Service

### Scheduled Jobs

**@Scheduled Tasks:**
1. **ScrapingScheduler** - Triggers scraping for enabled sources
   - Interval: Every 60 minutes per source (configurable)
   - Publishes ScrapeJobMessage to RabbitMQ

2. **EmailScheduler** - Sends daily top listings emails
   - Cron: `0 0 9 * * *` (9 AM daily)
   - Publishes EmailJobMessage to RabbitMQ

3. **CacheCleanupScheduler** - Removes stale cache entries
   - Cron: `0 0 3 * * *` (3 AM daily)

### Admin API

**Endpoints:**
```
POST   /api/admin/scrape/trigger                 - Trigger scraping for all sources
POST   /api/admin/scrape/trigger/{sourceName}    - Trigger scraping for specific source
GET    /api/admin/scrape/status                  - Get current scraping status
GET    /api/admin/scrape/history?page=&size=     - Get job history
POST   /api/admin/cache/invalidate               - Invalidate all cache
GET    /api/admin/sources                        - Get all sources
PUT    /api/admin/sources/{id}                   - Update source (enable/disable)
```

### Tasks
1. Create SchedulerConfig for enabling scheduling
2. Implement ScrapingScheduler
3. Implement EmailScheduler
4. Implement CacheCleanupScheduler
5. Create AdminController with endpoints
6. Create AdminService for business logic
7. Add validation and error handling
8. Secure admin endpoints (optional: Basic Auth or API Key)

### Deliverables
- Automatic scraping on schedule
- Admin API for manual triggers
- Job history tracking
- Source management

---

## Phase 7: Backend API for Frontend

### Public REST API

**Endpoints:**
```
GET /api/listings
  ?city=Bucharest
  &priceMin=50000
  &priceMax=200000
  &rooms=2,3
  &surfaceMin=50
  &surfaceMax=100
  &page=0
  &size=20
  &sort=price,asc

GET /api/listings/{id}

GET /api/listings/filters
  Response: {
    "cities": ["Bucharest", "Cluj-Napoca", ...],
    "priceRange": { "min": 30000, "max": 500000 },
    "surfaceRange": { "min": 20, "max": 300 },
    "roomOptions": [1, 2, 3, 4, 5, 6+]
  }

GET /api/sources
  Response: List of available real estate sources
```

### Implementation Details

**Filtering:**
- Spring Data JPA Specification for dynamic queries
- FilterSpecification builder pattern

**Caching:**
- Cache-aside pattern with Redis
- Hash filters to create unique cache keys

**DTOs with MapStruct:**
- `ListingDto` - frontend representation
- `ListingDetailDto` - detailed view
- `ListingFilterMetadataDto` - filter options

**Pagination:**
- Spring Data Page<T>
- Custom PageDto in gs-commons

### Tasks
1. Create ListingController in gs-orchestrator
2. Implement FilterSpecification for dynamic queries
3. Create ListingService with caching
4. Configure MapStruct mappers in gs-commons
5. Create DTOs for API responses
6. Implement filter metadata endpoint
7. Add CORS configuration for React frontend
8. Add request/response logging

### Deliverables
- RESTful API with filtering and pagination
- Cache-aside pattern implemented
- DTOs properly mapped
- CORS configured for frontend

---

## Phase 8: Email Notification System

### Email Features

**Email Types:**
1. **Top Listings Digest** - Daily email with best listings
2. **Price Alert** - When listing price drops (future)
3. **New Listings Alert** - New listings matching preferences (future)

**Template Engine:** Handlebars

### Email Service

**Configuration:**
- SMTP (Gmail, SendGrid, Mailgun, etc.)
- Async sending via RabbitMQ
- HTML templates with Handlebars

**Template Example (top-listings.hbs):**
```html
<html>
  <body>
    <h1>Top {{count}} Apartment Listings</h1>
    {{#each listings}}
      <div>
        <h2>{{title}}</h2>
        <p>Price: {{price}} {{currency}}</p>
        <p>Surface: {{surface}} m²</p>
        <a href="{{url}}">View Listing</a>
      </div>
    {{/each}}
  </body>
</html>
```

### Tasks
1. Configure Spring Mail in application.yml
2. Add Handlebars dependency
3. Create email templates in resources/templates/email/
4. Implement TemplateService for rendering Handlebars
5. Create EmailService for sending emails
6. Create @RabbitListener for email.queue in gs-worker
7. Implement top listings selection logic
8. Add email job tracking
9. Test email sending

### Deliverables
- Working email notification system
- Handlebars templates
- Async email sending via RabbitMQ
- Email job tracking

---

## Phase 9: Frontend - React UI

### Pages & Routes

1. **/ (Home/Listings Page)** - Main listings view with filters
2. **/listing/:id** - Detailed listing view
3. **/admin** - Admin panel for scraping triggers

### Components

**Layout:**
- `AppBar` - Header with logo, navigation
- `FilterPanel` - Side drawer or top panel with filters
- `Footer`

**Listing Components:**
- `ListingCard` - Card view for each listing
- `ListingGrid` - Grid layout using MUI Grid
- `ListingDetail` - Modal or separate page for details
- `FilterBar` - Active filters display with chips

**Admin Components:**
- `AdminDashboard` - Overview of scraping status
- `SourceTable` - MUI Table with source management
- `TriggerButton` - Manual scrape trigger
- `JobHistoryTable` - Recent scraping jobs

### State Management

**React Query:**
- `useListings` - Fetch paginated listings with filters
- `useListing` - Fetch single listing details
- `useFilters` - Fetch available filter options
- `useSources` - Fetch sources for admin
- `useTriggerScrape` - Mutation for triggering scrapes

**URL State:**
- Filters stored in URL query params
- Shareable filter links
- Browser back/forward support

### Services (Axios)

**API Client:**
```typescript
// src/services/api.ts
const apiClient = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  timeout: 10000,
});

// src/services/listingService.ts
export const listingService = {
  getListings: (filters: ListingFilters, page: number, size: number) => {...},
  getListing: (id: number) => {...},
  getFilterMetadata: () => {...},
};
```

### Styling

**MUI Theme:**
- Custom color palette (Romanian flag colors as accent?)
- Dark mode support
- Responsive breakpoints

### Tasks
1. Initialize React app with TypeScript (CRA or Vite)
2. Install dependencies (MUI, React Query, Axios, React Router)
3. Create project structure (components, pages, services, types)
4. Set up React Router
5. Create MUI theme configuration
6. Implement API service layer
7. Create TypeScript types/interfaces
8. Implement ListingCard component
9. Implement FilterPanel component
10. Implement Listings page with pagination
11. Implement ListingDetail page
12. Implement AdminDashboard page
13. Set up React Query for data fetching
14. Add loading states and error handling
15. Add responsive design

### Deliverables
- Fully functional React frontend
- Listings view with filters
- Admin panel
- Responsive design
- TypeScript types for all data

---

## Phase 10: Monitoring & Observability

### Spring Actuator Endpoints

**Enable:**
- `/actuator/health` - Health check
- `/actuator/metrics` - Prometheus metrics
- `/actuator/info` - App info

### Prometheus Metrics

**Custom Metrics:**
- `scraping_jobs_total` (counter) - by source, status
- `scraping_duration_seconds` (histogram) - by source
- `cache_hits_total` (counter)
- `cache_misses_total` (counter)
- `listings_scraped_total` (counter) - by source
- `email_sent_total` (counter) - by type, status

### Grafana Dashboards

**docker-compose.monitoring.yml:**
- Prometheus
- Grafana with pre-configured dashboards

**Dashboards:**
1. **System Overview** - CPU, memory, request rates
2. **Scraping Metrics** - Job success/failure, duration, listings scraped
3. **Cache Performance** - Hit/miss ratio, eviction rate
4. **API Performance** - Response times, error rates

### Tasks
1. Add Spring Actuator dependencies
2. Configure Actuator endpoints
3. Add Micrometer Prometheus registry
4. Implement custom metrics with @Timed, Counter, Gauge
5. Create docker-compose.monitoring.yml
6. Configure Prometheus scrape targets
7. Create Grafana dashboard JSON files
8. Set up Grafana data source
9. Document monitoring setup in README

### Deliverables
- Prometheus metrics exposed
- Grafana dashboards created
- Monitoring stack in Docker Compose
- Documentation for monitoring

---

## Phase 11: Docker & Deployment

### Dockerfiles

**gs-orchestrator & gs-worker (Multi-stage):**
```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY gs-commons/pom.xml gs-commons/
COPY gs-orchestrator/pom.xml gs-orchestrator/
RUN mvn dependency:go-offline

COPY gs-commons gs-commons/
COPY gs-orchestrator gs-orchestrator/
RUN mvn clean package -DskipTests -pl gs-orchestrator -am

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/gs-orchestrator/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**gs-ui (Multi-stage):**
```dockerfile
# Build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Runtime stage
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Docker Compose Files

**docker-compose.yml** - Full application stack:
- PostgreSQL
- Redis
- RabbitMQ
- gs-orchestrator
- gs-worker
- gs-ui
- nginx (reverse proxy, optional)

**docker-compose.monitoring.yml** - Monitoring:
- Prometheus
- Grafana

**Networks:**
- `app-network` - Application services
- `monitoring-network` - Monitoring services

**Volumes:**
- `postgres-data`
- `redis-data`
- `rabbitmq-data`
- `prometheus-data`
- `grafana-data`

### Environment Configuration

**.env file:**
```
POSTGRES_HOST=postgres
POSTGRES_DB=gimme_scrapes
POSTGRES_USER=gimmescrapes
POSTGRES_PASSWORD=***

REDIS_HOST=redis
REDIS_PORT=6379

RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=***

MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=***
MAIL_PASSWORD=***
```

### Tasks
1. Create Dockerfile for gs-orchestrator
2. Create Dockerfile for gs-worker
3. Create Dockerfile for gs-ui
4. Create nginx.conf for frontend
5. Create docker-compose.yml
6. Create docker-compose.monitoring.yml
7. Create .env.example file
8. Test full stack deployment
9. Add health checks to docker-compose
10. Document deployment in README

### Deliverables
- Working Dockerfiles for all services
- Complete docker-compose.yml
- Monitoring stack compose file
- Deployment documentation
- One-command startup: `docker-compose up -d`

---

## Testing Strategy

### Unit Tests
- Service layer with Mockito
- Scraper logic with mocked HTTP responses
- MapStruct mappers
- Utility classes

### Integration Tests
- Repository tests with @DataJpaTest
- RabbitMQ listeners with @SpringBootTest
- Redis caching with Testcontainers

### E2E Tests (Optional)
- Scraping pipeline end-to-end
- API endpoints with TestRestTemplate

### Test Coverage Goal
- Minimum 70% coverage for critical paths
- 100% coverage for scraper logic

---

## Performance Optimization

### Database
- Connection pooling (HikariCP)
- Index optimization based on query patterns
- Batch inserts for scraped listings

### Caching
- Redis for frequently accessed data
- Cache warming after scraping
- Proper TTL configuration

### API
- Pagination for all list endpoints
- Efficient query with Specification
- Response compression (GZIP)

### Scraping
- Parallel scraping with CompletableFuture (respect rate limits)
- Request throttling
- Connection pooling for HTTP client

---

## Security Considerations

### API Security
- CORS configuration for frontend
- Rate limiting (Spring Cloud Gateway or custom filter)
- Input validation with Bean Validation
- SQL injection prevention (JPA)

### Admin Panel
- Basic Auth or API Key for admin endpoints
- HTTPS in production
- Environment-based credentials

### Data Privacy
- Email validation
- Unsubscribe mechanism
- GDPR compliance (data retention policies)

---

## Deployment Checklist

- [ ] All environment variables configured
- [ ] Database migrations applied
- [ ] Redis memory limits set
- [ ] RabbitMQ queues created
- [ ] Email SMTP configured and tested
- [ ] Docker volumes for data persistence
- [ ] Health checks configured
- [ ] Monitoring dashboards imported
- [ ] Logging aggregation (optional: ELK stack)
- [ ] Backup strategy for PostgreSQL
- [ ] Domain and SSL certificates (production)

---

## Future Enhancements

1. **User Authentication** - Save filters, favorite listings
2. **Price History Tracking** - Chart price changes over time
3. **Email Alerts** - Notify on price drops or new listings
4. **Map View** - Display listings on interactive map
5. **Comparison Tool** - Compare multiple listings side-by-side
6. **Mobile App** - React Native or PWA
7. **Machine Learning** - Price prediction, listing quality scoring
8. **Multi-language** - Support for English, Romanian
9. **Social Sharing** - Share listings on social media
10. **Saved Searches** - Persistent filter presets

---

## Getting Started (Quick Reference)

### Prerequisites
- Java 21
- Maven 3.9+
- Node.js 20+
- Docker & Docker Compose

### Initial Setup
```bash
# Clone repository
git clone <repo-url>
cd gimme-scrapes

# Start infrastructure
docker-compose up -d postgres redis rabbitmq

# Build and run backend
mvn clean install
mvn spring-boot:run -pl gs-orchestrator
mvn spring-boot:run -pl gs-worker

# Run frontend
cd gs-ui
npm install
npm start

# Access applications
# Frontend: http://localhost:3000
# Orchestrator API: http://localhost:8080
# RabbitMQ Management: http://localhost:15672 (guest/guest)
```

---

## Timeline Estimate

- **Phase 1-2**: 2-3 days (Setup + Database)
- **Phase 3**: 1 day (RabbitMQ)
- **Phase 4**: 3-4 days (Scraping)
- **Phase 5**: 1-2 days (Caching)
- **Phase 6**: 1-2 days (Orchestrator)
- **Phase 7**: 2 days (API)
- **Phase 8**: 1-2 days (Email)
- **Phase 9**: 4-5 days (Frontend)
- **Phase 10**: 1-2 days (Monitoring)
- **Phase 11**: 2-3 days (Docker)

**Total**: ~20-27 days of focused development

---

## Success Metrics

- [ ] Scrapers successfully collect listings from 3+ Romanian sites
- [ ] Cache hit rate > 70%
- [ ] API response time < 200ms (with cache)
- [ ] Zero data loss during scraping
- [ ] Email delivery rate > 95%
- [ ] Application uptime > 99%
- [ ] Docker deployment works on any machine

---

**Document Version**: 1.0
**Last Updated**: 2025-11-02
