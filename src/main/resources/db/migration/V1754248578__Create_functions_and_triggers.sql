-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Trigger for products table
CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Function to calculate available inventory
CREATE OR REPLACE FUNCTION get_available_inventory(product_id_param BIGINT)
    RETURNS INTEGER AS $$
DECLARE
    available_qty INTEGER;
BEGIN
    SELECT (quantity - reserved_quantity) INTO available_qty
    FROM products
    WHERE id = product_id_param;

    RETURN COALESCE(available_qty, 0);
END;
$$ LANGUAGE plpgsql;

-- Function for atomic inventory updates
CREATE OR REPLACE FUNCTION update_inventory_atomic(
    product_id_param BIGINT,
    quantity_change INTEGER,
    reserved_change INTEGER DEFAULT 0
)
    RETURNS BOOLEAN AS $$
DECLARE
    current_qty INTEGER;
    current_reserved INTEGER;
    available_qty INTEGER;
BEGIN
    -- Lock the row for update
    SELECT quantity, reserved_quantity
    INTO current_qty, current_reserved
    FROM products
    WHERE id = product_id_param
        FOR UPDATE;

    IF NOT FOUND THEN
        RETURN FALSE;
    END IF;

    -- Calculate new values
    available_qty := (current_qty + quantity_change) - (current_reserved + reserved_change);

    -- Check if we have enough inventory
    IF available_qty < 0 THEN
        RETURN FALSE;
    END IF;

    -- Update the inventory
    UPDATE products
    SET quantity = quantity + quantity_change,
        reserved_quantity = reserved_quantity + reserved_change
    WHERE id = product_id_param;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql;