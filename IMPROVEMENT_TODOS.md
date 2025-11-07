# Improvement TODOs - Gimme Scrapes
Generated from Principal Engineer Review - 2025-11-07

## 🔴 CRITICAL PRIORITY (Do Before Phase 5)

### Testing Infrastructure
- [ ] Set up JUnit 5 and Mockito for unit testing
- [ ] Add Testcontainers for integration testing
- [ ] Configure JaCoCo for code coverage reporting
- [ ] **Target: Minimum 70% line coverage**
- [ ] Write unit tests for `ScraperService`
- [ ] Write unit tests for `ListingService`
- [ ] Write unit tests for all three scrapers (Storia, Imobiliare, OLX)
- [ ] Write integration tests for `ScrapeJobConsumer`
- [ ] Write integration tests for `ListingRepository` bulk operations
- [ ] Write integration tests for RabbitMQ message flow
- [ ] Add E2E test for complete scraping workflow
- [ ] Configure Maven to fail build on test failures
- [ ] Estimated effort: **2-3 weeks**

### Performance - Bulk Upsert Optimization
- [ ] Replace N+1 query pattern in `ListingService.bulkUpsert()`
- [ ] Implement PostgreSQL `INSERT ... ON CONFLICT DO UPDATE` using JdbcTemplate
- [ ] Configure Hibernate batch processing settings
  ```yaml
  spring.jpa.properties.hibernate.jdbc.batch_size: 50
  spring.jpa.properties.hibernate.order_inserts: true
  spring.jpa.properties.hibernate.order_updates: true
  ```
- [ ] Add performance tests to verify 10x improvement
- [ ] Benchmark before: ~3-5s for 100 listings
- [ ] Benchmark after: ~300ms for 100 listings
- [ ] **Location:** `gs-worker/src/main/java/com/deroahe/gimmescrapes/worker/service/ListingService.java:33`
- [ ] Estimated effort: **2 days**

### Error Handling Overhaul
- [ ] Change `ScrapingException` to extend `RuntimeException` instead of `Exception`
- [ ] Create `ErrorCode` enum with retryable/non-retryable classification
- [ ] Create exception hierarchy:
  - [ ] `GimmeScrapeException` (base)
  - [ ] `ScrapingException` (network, parse, timeout, rate-limit)
  - [ ] `ValidationException`
  - [ ] `DataPersistenceException`
- [ ] Add context map to exceptions for debugging
- [ ] Replace generic `catch (Exception e)` in scrapers with specific exceptions
- [ ] Implement retry logic based on `ErrorCode.isRetryable()`
- [ ] Add circuit breaker dependency (Resilience4j)
- [ ] Implement `@CircuitBreaker` on scraper methods
- [ ] Configure circuit breaker thresholds in application.yml
- [ ] **Locations:**
  - `gs-commons/src/main/java/com/deroahe/gimmescrapes/commons/exception/`
  - All scraper classes
- [ ] Estimated effort: **1 week**

---

## 🟠 HIGH PRIORITY (Complete During Phase 5)

### Security - Authentication & Authorization
- [ ] Add Spring Security dependency to gs-orchestrator
- [ ] Create `SecurityConfig` with HTTP Basic authentication
- [ ] Protect `/api/test/**` endpoints with `ROLE_ADMIN`
- [ ] Protect `/actuator/**` endpoints with `ROLE_ADMIN`
- [ ] Create application user accounts (externalize credentials)
- [ ] Add `@PreAuthorize` annotations to sensitive methods
- [ ] Test authentication with Postman/curl
- [ ] Document authentication in API docs
- [ ] **Location:** `gs-orchestrator/src/main/java/com/deroahe/gimmescrapes/orchestrator/`
- [ ] Estimated effort: **2 days**

### Security - Input Validation
- [ ] Add Bean Validation dependency (jakarta.validation)
- [ ] Add validation annotations to `ListingDto`:
  - [ ] `@NotBlank` on url
  - [ ] `@Size(max=500)` on url, title
  - [ ] `@DecimalMin("0.0")` on price, surfaceSqm
  - [ ] `@Min(1)` on rooms
- [ ] Add `@Valid` to controller method parameters
- [ ] Create custom validators for complex validation logic
- [ ] Add validation to `ScrapeJobMessage`
- [ ] Implement `@ControllerAdvice` for validation error handling
- [ ] Write tests for validation scenarios
- [ ] **Locations:**
  - `gs-commons/src/main/java/com/deroahe/gimmescrapes/commons/dto/`
  - `gs-orchestrator/src/main/java/com/deroahe/gimmescrapes/orchestrator/controller/`
- [ ] Estimated effort: **2 days**

### Code Quality - Thread Safety
- [ ] Replace `private final Random random = new Random()` with `ThreadLocalRandom`
- [ ] Update all scrapers:
  - [ ] `StoriaRoScraper.java:44`
  - [ ] `ImobiliareScraper.java:44`
  - [ ] `OlxScraper.java:44`
- [ ] Change usage from:
  ```java
  USER_AGENTS[random.nextInt(USER_AGENTS.length)]
  ```
  to:
  ```java
  USER_AGENTS[ThreadLocalRandom.current().nextInt(USER_AGENTS.length)]
  ```
- [ ] Add comment explaining thread-safety concern
- [ ] Estimated effort: **1 hour**

### Configuration - Externalize Scraper Settings
- [ ] Create `ScrapingProperties` class with `@ConfigurationProperties`
- [ ] Define properties in application.yml:
  - [ ] http.timeout-ms
  - [ ] http.user-agents list
  - [ ] sources.storia.max-pages
  - [ ] sources.storia.request-delay-ms
  - [ ] sources.storia.search (type, city, district, etc.)
  - [ ] Same for imobiliare and olx
- [ ] Remove hardcoded constants from scraper classes
- [ ] Inject `ScrapingProperties` into scrapers
- [ ] Make `buildSearchUrl()` use configuration
- [ ] Update tests to use test configuration
- [ ] **Locations:**
  - Create `gs-commons/src/main/java/com/deroahe/gimmescrapes/commons/config/ScrapingProperties.java`
  - Update all scraper classes
- [ ] Estimated effort: **2 days**

---

## 🟡 MEDIUM PRIORITY (Phase 5-6 Timeframe)

### Configuration Management
- [ ] Create environment-specific profiles:
  - [ ] `application-dev.yml`
  - [ ] `application-prod.yml`
  - [ ] `application-test.yml`
- [ ] Remove default passwords from application.yml
- [ ] Document required environment variables in README
- [ ] Create `.env.example` file with all required variables
- [ ] Add validation for required properties on startup
- [ ] Estimated effort: **1 day**

### RabbitMQ - Enhanced Retry Configuration
- [ ] Update RabbitMQ retry config in application.yml:
  - [ ] Set `max-interval: 60000` (1 minute cap)
  - [ ] Increase `max-attempts: 5`
  - [ ] Set `default-requeue-rejected: false`
- [ ] Configure message TTL for queues
- [ ] Set prefetch count for consumers
- [ ] Add explicit acknowledgment mode configuration
- [ ] Document retry behavior in code comments
- [ ] Estimated effort: **2 hours**

### Database - Data Integrity
- [ ] Create migration `V3__add_constraints.sql`
- [ ] Add composite unique constraint:
  ```sql
  ALTER TABLE listings ADD CONSTRAINT uk_listings_source_external_id
  UNIQUE (source_id, external_id);
  ```
- [ ] Add CHECK constraints:
  ```sql
  ALTER TABLE listings ADD CONSTRAINT chk_price_positive
  CHECK (price IS NULL OR price >= 0);

  ALTER TABLE listings ADD CONSTRAINT chk_surface_positive
  CHECK (surface_sqm IS NULL OR surface_sqm > 0);

  ALTER TABLE listings ADD CONSTRAINT chk_rooms_positive
  CHECK (rooms IS NULL OR rooms > 0);
  ```
- [ ] Test migration on local database
- [ ] Estimated effort: **2 hours**

### Code Quality - Reduce Duplication
- [ ] Extract common scraper configuration to `ScraperConfig`
- [ ] Create base abstract class `AbstractRealEstateScraper`
- [ ] Move common methods to base class:
  - [ ] `fetchDocument()`
  - [ ] `getUserAgent()`
  - [ ] `extractPrice()`
  - [ ] `extractCurrency()`
  - [ ] `extractNumber()`
  - [ ] `extractInteger()`
- [ ] Update all scrapers to extend base class
- [ ] Remove duplicated code
- [ ] Estimated effort: **1 day**

### Monitoring - Custom Metrics
- [ ] Add Micrometer `MeterRegistry` to services
- [ ] Implement custom metrics in `ScraperService`:
  - [ ] Counter: `scraping.success` (by source)
  - [ ] Counter: `scraping.failure` (by source)
  - [ ] Counter: `scraping.listings.found` (by source)
  - [ ] Timer: `scraping.duration` (by source)
- [ ] Implement custom metrics in `ListingService`:
  - [ ] Counter: `listings.upsert.new`
  - [ ] Counter: `listings.upsert.updated`
  - [ ] Counter: `listings.upsert.skipped`
  - [ ] Timer: `listings.upsert.duration`
- [ ] Add metrics to RabbitMQ consumers
- [ ] Create Grafana dashboard JSON template
- [ ] Estimated effort: **3 days**

### API Documentation
- [ ] Add SpringDoc OpenAPI dependency
- [ ] Add `@Tag` annotations to controllers
- [ ] Add `@Operation` and `@ApiResponse` annotations to endpoints
- [ ] Add `@Parameter` annotations to method parameters
- [ ] Add `@Schema` annotations to DTOs
- [ ] Configure OpenAPI info (title, version, description)
- [ ] Test Swagger UI at `/swagger-ui.html`
- [ ] Export OpenAPI spec for documentation
- [ ] Estimated effort: **2 days**

### Logging Improvements
- [ ] Update logging pattern in application.yml to include thread info
- [ ] Configure log levels per package
- [ ] Set up log file rotation
- [ ] Add MDC (Mapped Diagnostic Context) for trace IDs
- [ ] Add structured logging for key operations
- [ ] Estimated effort: **1 day**

### Security - Actuator Hardening
- [ ] Update actuator configuration to expose only necessary endpoints
- [ ] Set `management.endpoint.health.show-details: when-authorized`
- [ ] Protect actuator with Spring Security
- [ ] Create separate `ROLE_ACTUATOR` role
- [ ] Test actuator access control
- [ ] Estimated effort: **1 day**

---

## 🟢 LOW PRIORITY (Phase 7+ or Tech Debt Backlog)

### Performance - Parallel Scraping
- [ ] Add Guava dependency for RateLimiter
- [ ] Create configurable `ExecutorService` for parallel scraping
- [ ] Refactor scrapers to use `CompletableFuture` for page scraping
- [ ] Implement rate limiting with Guava `RateLimiter`
- [ ] Add configuration for concurrency level
- [ ] Benchmark performance improvement
- [ ] Expected improvement: 3-4x faster for multi-page scraping
- [ ] Estimated effort: **1 week**

### Performance - HTTP Connection Pooling
- [ ] Add Apache HttpComponents dependency
- [ ] Create `HttpClientProvider` bean
- [ ] Configure connection pool:
  - [ ] Max total connections: 100
  - [ ] Max per route: 20
  - [ ] Connection TTL: 30 seconds
- [ ] Update Jsoup usage to use custom HttpClient
- [ ] Add connection pool metrics
- [ ] Estimated effort: **2 days**

### Code Refactoring - MapStruct Usage
- [ ] Create MapStruct mappers for DTO ↔ Entity conversion
- [ ] Create `ListingMapper` interface
- [ ] Remove manual mapping in `ScraperService.convertToEntity()`
- [ ] Use `@Mapping` annotations for custom mappings
- [ ] Update `updateListing()` to use MapStruct with `IGNORE` null strategy
- [ ] Estimated effort: **2 days**

### Observability - Distributed Tracing
- [ ] Add Micrometer Tracing dependency
- [ ] Add Zipkin reporter dependency
- [ ] Configure tracing in application.yml
- [ ] Add span annotations to critical methods
- [ ] Set up Zipkin server in docker-compose
- [ ] Create tracing documentation
- [ ] Estimated effort: **1 week**

### Observability - Structured Logging
- [ ] Add Logstash Logback encoder dependency
- [ ] Configure JSON logging format
- [ ] Add MDC fields for correlation
- [ ] Use structured logging throughout application
- [ ] Set up ELK stack for log aggregation (optional)
- [ ] Estimated effort: **3 days**

### Database - Performance Optimization
- [ ] Analyze slow queries with pg_stat_statements
- [ ] Create composite indexes based on query patterns
- [ ] Consider table partitioning for listings (by date or source)
- [ ] Implement database connection pool monitoring
- [ ] Add query hints for complex queries
- [ ] Estimated effort: **1 week**

### Security - Rate Limiting
- [ ] Add Bucket4j dependency
- [ ] Create `RateLimitingInterceptor`
- [ ] Configure rate limits:
  - [ ] Public API: 100 req/min per IP
  - [ ] Test API: 10 req/min per IP
  - [ ] Admin API: 30 req/min per user
- [ ] Add rate limit headers to responses
- [ ] Create rate limit exceeded handler
- [ ] Test with load testing tool
- [ ] Estimated effort: **2 days**

### Documentation
- [ ] Add comprehensive JavaDoc to all public APIs
- [ ] Create architecture decision records (ADRs)
- [ ] Document scraping strategy for each source
- [ ] Create troubleshooting guide
- [ ] Add performance tuning guide
- [ ] Create deployment guide
- [ ] Estimated effort: **1 week**

---

## Testing Breakdown (Detailed)

### Unit Tests - Week 1
- [ ] `ScraperServiceTest`
  - [ ] Test scraper selection by source name
  - [ ] Test behavior when no scraper found
  - [ ] Test DTO to entity conversion
  - [ ] Test getAvailableScrapers()
- [ ] `ListingServiceTest`
  - [ ] Test upsert new listings
  - [ ] Test upsert existing listings
  - [ ] Test skipping invalid listings
  - [ ] Test update detection logic
- [ ] `StoriaRoScraperTest`
  - [ ] Test JSON extraction from __NEXT_DATA__
  - [ ] Test listing parsing
  - [ ] Test room enum conversion
  - [ ] Test floor enum conversion
  - [ ] Test error handling
- [ ] `ImobiliareScraperTest`
  - [ ] Test listing card extraction
  - [ ] Test price parsing
  - [ ] Test location parsing
  - [ ] Test error handling
- [ ] `OlxScraperTest`
  - [ ] Test listing card extraction
  - [ ] Test external ID extraction
  - [ ] Test attribute parsing
  - [ ] Test error handling

### Integration Tests - Week 2
- [ ] `ListingRepositoryIntegrationTest`
  - [ ] Test findByUrl()
  - [ ] Test bulk save operations
  - [ ] Test unique constraint violations
  - [ ] Use @DataJpaTest with Testcontainers
- [ ] `ScrapeJobConsumerIntegrationTest`
  - [ ] Test complete message consumption flow
  - [ ] Test job status updates
  - [ ] Test error handling and retry
  - [ ] Use @SpringBootTest with Testcontainers (PostgreSQL + RabbitMQ)
- [ ] `RabbitMQIntegrationTest`
  - [ ] Test message publishing
  - [ ] Test DLQ routing
  - [ ] Test retry mechanism
  - [ ] Use Testcontainers RabbitMQ

### E2E Tests - Week 3
- [ ] `ScrapeFlowE2ETest`
  - [ ] Trigger scrape via TestController
  - [ ] Verify job creation
  - [ ] Verify listings saved to database
  - [ ] Verify job completion status
  - [ ] Mock external HTTP calls with WireMock

---

## Estimation Summary

| Category | Tasks | Estimated Effort | Priority |
|----------|-------|------------------|----------|
| Testing Infrastructure | 12 | 2-3 weeks | 🔴 CRITICAL |
| Performance Optimization | 2 | 2 days | 🔴 CRITICAL |
| Error Handling | 11 | 1 week | 🔴 CRITICAL |
| Security | 15 | 4-5 days | 🟠 HIGH |
| Configuration | 10 | 3-4 days | 🟠 HIGH |
| Code Quality | 8 | 2-3 days | 🟡 MEDIUM |
| Monitoring | 9 | 3-4 days | 🟡 MEDIUM |
| Documentation | 8 | 2 days | 🟡 MEDIUM |
| Advanced Performance | 8 | 1-2 weeks | 🟢 LOW |
| Advanced Observability | 7 | 1-2 weeks | 🟢 LOW |

**Total Estimated Effort for Critical + High Priority:** 4-5 weeks

---

## Sprint Planning Suggestion

### Sprint 1 (Week 1-2): Testing Foundation
- Set up testing infrastructure
- Write unit tests for services and scrapers
- Achieve 50% code coverage

### Sprint 2 (Week 2-3): Testing Completion + Performance
- Write integration and E2E tests
- Achieve 70% code coverage
- Optimize bulk upsert (critical performance fix)

### Sprint 3 (Week 3-4): Error Handling + Security
- Overhaul exception hierarchy
- Implement circuit breaker
- Add authentication and input validation
- Fix thread-safety issues

### Sprint 4 (Week 4-5): Configuration + Polish
- Externalize scraper configuration
- Add custom metrics
- Improve logging
- Harden actuator security
- Code review and cleanup

### Sprint 5 (Week 5-6): Phase 5 Prep + Medium Priority
- Database constraints
- RabbitMQ improvements
- API documentation
- Code deduplication
- Ready for Phase 5 (Redis caching)

---

## Metrics to Track

### Code Quality Metrics
- [ ] Test coverage: Current 0% → Target 70%+
- [ ] Code duplication: Measure with SonarQube
- [ ] Cyclomatic complexity: Max 10 per method
- [ ] Technical debt ratio: < 5%

### Performance Metrics
- [ ] Bulk upsert 100 listings: Current ~3-5s → Target <500ms
- [ ] Scrape 30 pages: Current ~60s → Target <20s
- [ ] API response time (cached): < 50ms
- [ ] API response time (uncached): < 200ms

### Reliability Metrics
- [ ] Test pass rate: 100%
- [ ] Build success rate: > 95%
- [ ] Code review approval rate: 100%
- [ ] Zero critical security vulnerabilities

---

## Notes
- All tasks should create a separate branch and pull request
- Each PR should include tests
- Each PR should pass CI/CD checks (once configured)
- Consider pairing for complex tasks (e.g., bulk upsert optimization)
- Schedule code review sessions for architectural changes
- Update this TODO list as tasks are completed

**Last Updated:** 2025-11-07
**Next Review:** After Sprint 2 completion
