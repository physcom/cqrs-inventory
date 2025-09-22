package kg.akyl.java.inventory.query.handlers;

import kg.akyl.java.inventory.query.projections.InventoryStatusProjection;
import kg.akyl.java.inventory.query.projections.ProductProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProductQueryHandler {
    @Autowired
    @Qualifier("readJdbcTemplate")
    private JdbcTemplate readJdbcTemplate;

    @Cacheable(value = "products", key = "#id")
    public Optional<ProductProjection> findById(Long id) {
        String sql = """
            SELECT p.id, p.sku, p.name, p.description, p.price, 
                   p.quantity, p.reserved_quantity, p.category, 
                   p.status, p.created_at, p.updated_at
            FROM products p 
            WHERE p.id = ? AND p.status != 'DISCONTINUED'
            """;

        try {
            ProductProjection product = readJdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> new ProductProjection(
                            rs.getLong("id"),
                            rs.getString("sku"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getBigDecimal("price"),
                            rs.getInt("quantity"),
                            rs.getInt("reserved_quantity"),
                            rs.getString("category"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    ), id);
            return Optional.of(product);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Cacheable(value = "products", key = "#sku")
    public Optional<ProductProjection> findBySku(String sku) {
        String sql = """
            SELECT p.id, p.sku, p.name, p.description, p.price, 
                   p.quantity, p.reserved_quantity, p.category, 
                   p.status, p.created_at, p.updated_at
            FROM products p 
            WHERE p.sku = ? AND p.status != 'DISCONTINUED'
            """;

        try {
            ProductProjection product = readJdbcTemplate.queryForObject(sql,
                    (rs, rowNum) -> new ProductProjection(
                            rs.getLong("id"),
                            rs.getString("sku"),
                            rs.getString("name"),
                            rs.getString("description"),
                            rs.getBigDecimal("price"),
                            rs.getInt("quantity"),
                            rs.getInt("reserved_quantity"),
                            rs.getString("category"),
                            rs.getString("status"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime()
                    ), sku);
            return Optional.of(product);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<ProductProjection> findByCategory(String category, int limit, int offset) {
        String sql = """
            SELECT p.id, p.sku, p.name, p.description, p.price, 
                   p.quantity, p.reserved_quantity, p.category, 
                   p.status, p.created_at, p.updated_at
            FROM products p 
            WHERE p.category = ? AND p.status = 'ACTIVE'
            ORDER BY p.name
            LIMIT ? OFFSET ?
            """;

        return readJdbcTemplate.query(sql,
                (rs, rowNum) -> new ProductProjection(
                        rs.getLong("id"),
                        rs.getString("sku"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity"),
                        rs.getInt("reserved_quantity"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ), category, limit, offset);
    }

    public List<InventoryStatusProjection> getLowStockProducts(int threshold) {
        String sql = """
            SELECT p.id, p.sku, p.name, p.quantity, p.reserved_quantity,
                   (p.quantity - p.reserved_quantity) as available_quantity
            FROM products p 
            WHERE (p.quantity - p.reserved_quantity) <= ? 
            AND p.status = 'ACTIVE'
            ORDER BY available_quantity ASC
            """;

        return readJdbcTemplate.query(sql,
                (rs, rowNum) -> new InventoryStatusProjection(
                        rs.getLong("id"),
                        rs.getString("sku"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getInt("reserved_quantity"),
                        rs.getInt("available_quantity")
                ), threshold);
    }

    public List<ProductProjection> searchProducts(String searchTerm, int limit, int offset) {
        String sql = """
            SELECT p.id, p.sku, p.name, p.description, p.price,
                   p.quantity, p.reserved_quantity, p.category,
                   p.status, p.created_at, p.updated_at
            FROM products p
            WHERE (p.name ILIKE ? OR p.description ILIKE ? OR p.sku ILIKE ? OR p.category ILIKE ?)
            AND p.status = 'ACTIVE'
            ORDER BY
                CASE
                    WHEN p.name ILIKE ? THEN 1
                    WHEN p.sku ILIKE ? THEN 2
                    WHEN p.category ILIKE ? THEN 3
                    ELSE 4
                END,
                p.name
            LIMIT ? OFFSET ?
            """;

        String searchPattern = "%" + searchTerm + "%";
        String exactPattern = searchTerm + "%";

        return readJdbcTemplate.query(sql,
                (rs, rowNum) -> new ProductProjection(
                        rs.getLong("id"),
                        rs.getString("sku"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getInt("quantity"),
                        rs.getInt("reserved_quantity"),
                        rs.getString("category"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ), searchPattern, searchPattern, searchPattern, searchPattern, exactPattern, exactPattern, exactPattern, limit, offset);
    }
}
