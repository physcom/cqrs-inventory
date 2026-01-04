

-- ============================================
-- SUPPLIERS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS suppliers (
    -- Primary Key
                                         id BIGSERIAL PRIMARY KEY,

    -- Core Fields
                                         supplier_code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    supplier_type VARCHAR(50) NOT NULL CHECK (
                                                 supplier_type IN ('MANUFACTURER', 'DISTRIBUTOR', 'WHOLESALER', 'RETAILER', 'DROPSHIPPER', 'AGENT')
    ),

    -- Contact Information
    email VARCHAR(100),
    phone VARCHAR(20),
    contact_person VARCHAR(100),

    -- Address Information
    address TEXT,
    city VARCHAR(100),
    country VARCHAR(100),
    postal_code VARCHAR(20),

    -- Business Information
    tax_id VARCHAR(50),
    credit_limit DECIMAL(15,2) DEFAULT 0.00 CHECK (credit_limit >= 0),
    payment_terms_days INTEGER DEFAULT 30 CHECK (payment_terms_days >= 0),

    -- Performance Metrics
    rating DECIMAL(3,2) CHECK (rating >= 0 AND rating <= 5.00),

    -- Status
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (
                                                           status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING_APPROVAL')
    ),

    -- Notes
    notes TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Optimistic Locking
    version BIGINT DEFAULT 0
    );

-- ============================================
-- SUPPLY CHAIN TRANSACTIONS TABLE
-- ============================================
CREATE TABLE IF NOT EXISTS supply_chain_transactions (
    -- Primary Key
                                                         id BIGSERIAL PRIMARY KEY,

    -- Transaction Identifier
                                                         transaction_number VARCHAR(50) NOT NULL UNIQUE,

    -- Supplier Relationship
    from_supplier_id BIGINT NOT NULL,
    to_supplier_id BIGINT NOT NULL,

    -- Product Reference
    product_id BIGINT NOT NULL,

    -- Transaction Details
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price > 0),
    total_amount DECIMAL(15,2) NOT NULL CHECK (total_amount >= 0),

    -- Transaction Metadata
    transaction_type VARCHAR(20) NOT NULL DEFAULT 'SALE' CHECK (
                                                                   transaction_type IN ('SALE', 'PURCHASE', 'TRANSFER', 'RETURN')
    ),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (
                                                            status IN ('PENDING', 'CONFIRMED', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED', 'REFUNDED')
    ),

    -- Date Tracking
    transaction_date TIMESTAMP NOT NULL,
    expected_delivery_date TIMESTAMP,
    actual_delivery_date TIMESTAMP,

    -- Document References
    invoice_number VARCHAR(50),
    purchase_order_number VARCHAR(50),

    -- Financial Details
    shipping_cost DECIMAL(10,2) DEFAULT 0.00 CHECK (shipping_cost >= 0),
    tax_amount DECIMAL(10,2) DEFAULT 0.00 CHECK (tax_amount >= 0),
    discount_amount DECIMAL(10,2) DEFAULT 0.00 CHECK (discount_amount >= 0),

    -- Payment Information
    payment_method VARCHAR(20),
    payment_date TIMESTAMP,

    -- Notes
    notes TEXT,

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Optimistic Locking
    version BIGINT DEFAULT 0,

    -- Foreign Key Constraints
    CONSTRAINT fk_from_supplier
    FOREIGN KEY (from_supplier_id)
    REFERENCES suppliers(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,

    CONSTRAINT fk_to_supplier
    FOREIGN KEY (to_supplier_id)
    REFERENCES suppliers(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,

    CONSTRAINT fk_product
    FOREIGN KEY (product_id)
    REFERENCES products(id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,

    -- Business Rules
    CONSTRAINT chk_different_suppliers
    CHECK (from_supplier_id != to_supplier_id),

    CONSTRAINT chk_delivery_dates
    CHECK (
    actual_delivery_date IS NULL OR
    expected_delivery_date IS NULL OR
    actual_delivery_date >= transaction_date
          )
    );

-- ============================================
-- INDEXES FOR SUPPLIERS TABLE
-- ============================================

-- Unique index on supplier code (already enforced by UNIQUE constraint)
CREATE UNIQUE INDEX IF NOT EXISTS idx_supplier_code
    ON suppliers(supplier_code);

-- Index on supplier name for search
CREATE INDEX IF NOT EXISTS idx_supplier_name
    ON suppliers(name);

-- Index on supplier type for filtering
CREATE INDEX IF NOT EXISTS idx_supplier_type
    ON suppliers(supplier_type);

-- Index on supplier status for filtering
CREATE INDEX IF NOT EXISTS idx_supplier_status
    ON suppliers(status);

-- Index on country for geographic queries
CREATE INDEX IF NOT EXISTS idx_supplier_country
    ON suppliers(country);

-- Index on city for geographic queries
CREATE INDEX IF NOT EXISTS idx_supplier_city
    ON suppliers(city);

-- Index on rating for performance queries
CREATE INDEX IF NOT EXISTS idx_supplier_rating
    ON suppliers(rating DESC)
    WHERE rating IS NOT NULL;

-- Composite index for active suppliers by type
CREATE INDEX IF NOT EXISTS idx_supplier_active_type
    ON suppliers(status, supplier_type)
    WHERE status = 'ACTIVE';

-- Index on created_at for temporal queries
CREATE INDEX IF NOT EXISTS idx_supplier_created_at
    ON suppliers(created_at DESC);

-- ============================================
-- INDEXES FOR SUPPLY CHAIN TRANSACTIONS TABLE
-- ============================================

-- Unique index on transaction number (already enforced by UNIQUE constraint)
CREATE UNIQUE INDEX IF NOT EXISTS idx_transaction_number
    ON supply_chain_transactions(transaction_number);

-- Index on from_supplier_id for outbound transaction queries
CREATE INDEX IF NOT EXISTS idx_from_supplier
    ON supply_chain_transactions(from_supplier_id);

-- Index on to_supplier_id for inbound transaction queries
CREATE INDEX IF NOT EXISTS idx_to_supplier
    ON supply_chain_transactions(to_supplier_id);

-- Index on product_id for product transaction history
CREATE INDEX IF NOT EXISTS idx_transaction_product
    ON supply_chain_transactions(product_id);

-- Index on transaction_date for temporal queries
CREATE INDEX IF NOT EXISTS idx_transaction_date
    ON supply_chain_transactions(transaction_date DESC);

-- Index on status for filtering
CREATE INDEX IF NOT EXISTS idx_transaction_status
    ON supply_chain_transactions(status);

-- Index on transaction_type for filtering
CREATE INDEX IF NOT EXISTS idx_transaction_type
    ON supply_chain_transactions(transaction_type);

-- Composite index for supplier connections analysis
CREATE INDEX IF NOT EXISTS idx_supplier_connections
    ON supply_chain_transactions(from_supplier_id, to_supplier_id);

-- Composite index for active transactions by status and date
CREATE INDEX IF NOT EXISTS idx_active_transactions
    ON supply_chain_transactions(status, transaction_date DESC)
    WHERE status NOT IN ('CANCELLED', 'DELIVERED');

-- Index on expected_delivery_date for overdue tracking
CREATE INDEX IF NOT EXISTS idx_expected_delivery
    ON supply_chain_transactions(expected_delivery_date)
    WHERE status IN ('CONFIRMED', 'IN_TRANSIT') AND expected_delivery_date IS NOT NULL;

-- Composite index for financial reporting
CREATE INDEX IF NOT EXISTS idx_transaction_financial
    ON supply_chain_transactions(transaction_date, status, total_amount)
    WHERE status != 'CANCELLED';

-- Index on created_at for audit queries
CREATE INDEX IF NOT EXISTS idx_transaction_created_at
    ON supply_chain_transactions(created_at DESC);

-- ============================================
-- TRIGGERS FOR AUTOMATIC TIMESTAMP UPDATES
-- ============================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for suppliers table
DROP TRIGGER IF EXISTS trigger_suppliers_updated_at ON suppliers;
CREATE TRIGGER trigger_suppliers_updated_at
    BEFORE UPDATE ON suppliers
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for supply_chain_transactions table
DROP TRIGGER IF EXISTS trigger_transactions_updated_at ON supply_chain_transactions;
CREATE TRIGGER trigger_transactions_updated_at
    BEFORE UPDATE ON supply_chain_transactions
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================
-- COMMENTS FOR DOCUMENTATION
-- ============================================

-- Table comments
COMMENT ON TABLE suppliers IS 'Stores supplier information including manufacturers, distributors, wholesalers, retailers, dropshippers, and agents';
COMMENT ON TABLE supply_chain_transactions IS 'Tracks supplier-to-supplier product transactions with complete financial and delivery information';

-- Column comments for suppliers
COMMENT ON COLUMN suppliers.supplier_code IS 'Unique identifier code for the supplier';
COMMENT ON COLUMN suppliers.supplier_type IS 'Type of supplier: MANUFACTURER, DISTRIBUTOR, WHOLESALER, RETAILER, DROPSHIPPER, AGENT';
COMMENT ON COLUMN suppliers.credit_limit IS 'Maximum credit limit allowed for this supplier in base currency';
COMMENT ON COLUMN suppliers.payment_terms_days IS 'Standard payment terms in days (e.g., Net 30)';
COMMENT ON COLUMN suppliers.rating IS 'Supplier performance rating from 0.00 to 5.00';
COMMENT ON COLUMN suppliers.status IS 'Current status: ACTIVE, INACTIVE, SUSPENDED, PENDING_APPROVAL';
COMMENT ON COLUMN suppliers.version IS 'Version number for optimistic locking';

-- Column comments for transactions
COMMENT ON COLUMN supply_chain_transactions.transaction_number IS 'Unique transaction identifier';
COMMENT ON COLUMN supply_chain_transactions.from_supplier_id IS 'Supplier selling/transferring the product';
COMMENT ON COLUMN supply_chain_transactions.to_supplier_id IS 'Supplier receiving the product';
COMMENT ON COLUMN supply_chain_transactions.transaction_type IS 'Type: SALE, PURCHASE, TRANSFER, RETURN';
COMMENT ON COLUMN supply_chain_transactions.status IS 'Status: PENDING, CONFIRMED, IN_TRANSIT, DELIVERED, CANCELLED, REFUNDED';
COMMENT ON COLUMN supply_chain_transactions.total_amount IS 'Total transaction amount including shipping, tax, minus discounts';
COMMENT ON COLUMN supply_chain_transactions.version IS 'Version number for optimistic locking';

-- ============================================
-- SAMPLE DATA INSERTION (OPTIONAL)
-- ============================================

-- Insert sample suppliers
INSERT INTO suppliers (supplier_code, name, supplier_type, email, phone, rating, status, city, country) VALUES
('SUP-001', 'TechCorp Manufacturing', 'MANUFACTURER', 'contact@techcorp.com', '+1-555-0101', 4.80, 'ACTIVE', 'San Francisco', 'USA'),
('SUP-002', 'Global Distributors Inc', 'DISTRIBUTOR', 'sales@globaldist.com', '+1-555-0102', 4.50, 'ACTIVE', 'New York', 'USA'),
('SUP-003', 'Electronics Wholesale Ltd', 'WHOLESALER', 'info@elecwholesale.com', '+1-555-0103', 4.30, 'ACTIVE', 'Chicago', 'USA'),
('SUP-004', 'Prime Retail Chain', 'RETAILER', 'orders@primeretail.com', '+1-555-0104', 4.60, 'ACTIVE', 'Los Angeles', 'USA'),
('SUP-005', 'FastShip Dropshippers', 'DROPSHIPPER', 'support@fastship.com', '+1-555-0105', 4.20, 'ACTIVE', 'Seattle', 'USA'),
('SUP-006', 'AsiaConnect Manufacturers', 'MANUFACTURER', 'export@asiaconnect.com', '+86-555-0106', 4.70, 'ACTIVE', 'Shanghai', 'China'),
('SUP-007', 'Euro Distribution Network', 'DISTRIBUTOR', 'contact@eurodist.com', '+49-555-0107', 4.40, 'ACTIVE', 'Berlin', 'Germany'),
('SUP-008', 'Pacific Trading Co', 'AGENT', 'agent@pacifictrade.com', '+81-555-0108', 4.10, 'ACTIVE', 'Tokyo', 'Japan')
ON CONFLICT (supplier_code) DO NOTHING;

-- Note: Sample transaction data requires existing products
-- Uncomment and adjust product_id values after products table is populated
/*
INSERT INTO supply_chain_transactions (
    transaction_number, from_supplier_id, to_supplier_id, product_id,
    quantity, unit_price, total_amount, transaction_type, status, transaction_date
) VALUES
('TXN-2024-001', 1, 2, 1, 500, 85.00, 42500.00, 'SALE', 'DELIVERED', '2024-01-15 10:30:00'),
('TXN-2024-002', 2, 3, 1, 300, 89.00, 26700.00, 'SALE', 'DELIVERED', '2024-01-18 14:20:00'),
('TXN-2024-003', 3, 4, 1, 200, 92.00, 18400.00, 'SALE', 'IN_TRANSIT', '2024-01-20 09:15:00'),
('TXN-2024-004', 6, 2, 2, 1000, 230.00, 230000.00, 'SALE', 'DELIVERED', '2024-01-10 11:00:00'),
('TXN-2024-005', 2, 7, 2, 400, 245.00, 98000.00, 'SALE', 'CONFIRMED', '2024-01-22 16:45:00')
ON CONFLICT (transaction_number) DO NOTHING;
*/

-- ============================================
-- VERIFICATION QUERIES
-- ============================================

-- Count suppliers by type
-- SELECT supplier_type, COUNT(*) as count FROM suppliers GROUP BY supplier_type;

-- Count transactions by status
-- SELECT status, COUNT(*) as count FROM supply_chain_transactions GROUP BY status;

-- Get supplier network connections
-- SELECT
--     fs.name as from_supplier,
--     ts.name as to_supplier,
--     COUNT(*) as transaction_count,
--     SUM(total_amount) as total_value
-- FROM supply_chain_transactions t
-- JOIN suppliers fs ON t.from_supplier_id = fs.id
-- JOIN suppliers ts ON t.to_supplier_id = ts.id
-- WHERE t.status != 'CANCELLED'
-- GROUP BY fs.name, ts.name
-- ORDER BY total_value DESC;