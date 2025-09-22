-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS pg_stat_statements;

-- Optimize PostgreSQL settings (add to postgresql.conf)
/*
# Memory settings
shared_buffers = 256MB                  # 25% of RAM for dedicated server
effective_cache_size = 1GB              # 75% of RAM
work_mem = 4MB                          # Per connection sort memory
maintenance_work_mem = 64MB             # For maintenance operations

# Checkpoint settings
checkpoint_completion_target = 0.9
wal_buffers = 16MB
wal_writer_delay = 200ms

# Query planner
random_page_cost = 1.1                  # SSD optimized
effective_io_concurrency = 200          # SSD concurrency

# Connection settings
max_connections = 200
shared_preload_libraries = 'pg_stat_statements'

# Logging for monitoring
log_statement = 'mod'                   # Log modifications
log_min_duration_statement = 1000       # Log slow queries
log_checkpoints = on
log_connections = on
log_disconnections = on
*/

-- Analyze tables for better query planning
ANALYZE products;
ANALYZE sales;
ANALYZE event_store;

-- Create materialized views for analytics
CREATE MATERIALIZED VIEW mv_daily_sales AS
SELECT
    DATE(sale_date) as sale_date,
    p.category,
    COUNT(*) as total_sales,
    SUM(s.quantity) as total_quantity,
    SUM(s.total_amount) as total_revenue
FROM sales s
    JOIN products p ON s.product_id = p.id
WHERE s.status = 'CONFIRMED'
GROUP BY DATE(sale_date), p.category;

CREATE UNIQUE INDEX idx_mv_daily_sales ON mv_daily_sales(sale_date, category);

-- Refresh materialized view function
CREATE OR REPLACE FUNCTION refresh_daily_sales_mv()
RETURNS VOID AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY mv_daily_sales;
END;
$$ LANGUAGE plpgsql;