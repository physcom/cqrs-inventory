-- Create sequences with high allocation for performance
CREATE SEQUENCE product_sequence
    START WITH 1
    INCREMENT BY 50
    CACHE 50;

CREATE SEQUENCE sale_sequence
    START WITH 1
    INCREMENT BY 50
    CACHE 50;

-- Products table
CREATE TABLE products (
                          id BIGINT PRIMARY KEY DEFAULT nextval('product_sequence'),
                          sku VARCHAR(100) UNIQUE NOT NULL,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          price DECIMAL(10,2) NOT NULL,
                          quantity INTEGER NOT NULL DEFAULT 0,
                          reserved_quantity INTEGER NOT NULL DEFAULT 0,
                          category VARCHAR(100),
                          status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                          version BIGINT NOT NULL DEFAULT 0,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Sales table
CREATE TABLE sales (
                       id BIGINT PRIMARY KEY DEFAULT nextval('sale_sequence'),
                       product_id BIGINT NOT NULL,
                       quantity INTEGER NOT NULL,
                       unit_price DECIMAL(10,2) NOT NULL,
                       total_amount DECIMAL(12,2) NOT NULL,
                       customer_id VARCHAR(100),
                       status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                       sale_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Event store table for CQRS
CREATE TABLE event_store (
                             id VARCHAR(36) PRIMARY KEY,
                             event_type VARCHAR(100) NOT NULL,
                             aggregate_id VARCHAR(100) NOT NULL,
                             event_data JSONB NOT NULL,
                             timestamp TIMESTAMP NOT NULL,
                             version BIGINT NOT NULL
);