-- Product indexes for high performance
CREATE INDEX if not exists idx_product_sku ON products(sku);
CREATE INDEX if not exists idx_product_category ON products(category);
CREATE INDEX if not exists  idx_product_status ON products(status);
CREATE INDEX if not exists idx_product_quantity ON products(quantity);
CREATE INDEX if not exists idx_product_available_qty ON products((quantity - reserved_quantity));

-- Sale indexes
CREATE INDEX if not exists idx_sale_product ON sales(product_id);
CREATE INDEX if not exists  idx_sale_date ON sales(sale_date);
CREATE INDEX if not exists idx_sale_status ON sales(status);
CREATE INDEX if not exists idx_sale_customer ON sales(customer_id);
CREATE INDEX if not exists idx_sale_date_status ON sales(sale_date, status);

-- Event store indexes
CREATE INDEX if not exists idx_event_aggregate ON event_store(aggregate_id);
CREATE INDEX if not exists idx_event_type ON event_store(event_type);
CREATE INDEX if not exists idx_event_timestamp ON event_store(timestamp);
CREATE INDEX if not exists idx_event_type_timestamp ON event_store(event_type, timestamp);

-- Composite indexes for analytics
CREATE INDEX if not exists idx_sales_analytics ON sales(sale_date, status, product_id);
CREATE INDEX if not exists idx_product_category_status ON products(category, status);