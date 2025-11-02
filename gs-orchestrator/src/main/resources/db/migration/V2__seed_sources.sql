-- Seed Romanian real estate sources
INSERT INTO sources (name, display_name, base_url, enabled, scrape_interval_minutes, created_at, updated_at)
VALUES
    ('imobiliare.ro', 'Imobiliare.ro', 'https://www.imobiliare.ro', true, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('olx.ro', 'OLX.ro', 'https://www.olx.ro/imobiliare', true, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('storia.ro', 'Storia.ro', 'https://www.storia.ro', true, 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
