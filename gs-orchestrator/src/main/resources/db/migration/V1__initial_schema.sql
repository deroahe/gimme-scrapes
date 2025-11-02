-- Create sources table
CREATE TABLE sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    display_name VARCHAR(255),
    base_url VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT true,
    scrape_interval_minutes INTEGER,
    last_scrape_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create listings table
CREATE TABLE listings (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL,
    external_id VARCHAR(255),
    url VARCHAR(500) UNIQUE NOT NULL,
    title VARCHAR(500),
    description TEXT,
    price DECIMAL(12, 2),
    currency VARCHAR(10) DEFAULT 'EUR',
    surface_sqm DECIMAL(10, 2),
    price_per_sqm DECIMAL(10, 2),
    rooms INTEGER,
    bathrooms INTEGER,
    floor INTEGER,
    total_floors INTEGER,
    year_built INTEGER,
    city VARCHAR(255),
    neighborhood VARCHAR(255),
    address VARCHAR(500),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    image_urls TEXT[],
    features JSONB,
    first_scraped_at TIMESTAMP,
    last_scraped_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_listings_source FOREIGN KEY (source_id) REFERENCES sources(id) ON DELETE CASCADE
);

-- Create indexes for listings
CREATE INDEX idx_listings_city ON listings(city);
CREATE INDEX idx_listings_price ON listings(price);
CREATE INDEX idx_listings_price_per_sqm ON listings(price_per_sqm);
CREATE INDEX idx_listings_scraped ON listings(last_scraped_at);
CREATE INDEX idx_listings_source ON listings(source_id);
CREATE INDEX idx_listings_url ON listings(url);

-- Create scraping_jobs table
CREATE TABLE scraping_jobs (
    id BIGSERIAL PRIMARY KEY,
    source_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    items_scraped INTEGER DEFAULT 0,
    items_new INTEGER DEFAULT 0,
    items_updated INTEGER DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_scraping_jobs_source FOREIGN KEY (source_id) REFERENCES sources(id) ON DELETE CASCADE
);

-- Create indexes for scraping_jobs
CREATE INDEX idx_scraping_jobs_source ON scraping_jobs(source_id);
CREATE INDEX idx_scraping_jobs_status ON scraping_jobs(status);

-- Create email_subscriptions table
CREATE TABLE email_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    preferences JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Create email_jobs table
CREATE TABLE email_jobs (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT,
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(500),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_email_jobs_subscription FOREIGN KEY (subscription_id) REFERENCES email_subscriptions(id) ON DELETE SET NULL
);
