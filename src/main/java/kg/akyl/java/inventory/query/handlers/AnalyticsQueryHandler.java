package kg.akyl.java.inventory.query.handlers;

import kg.akyl.java.inventory.query.projections.InventoryAnalyticsProjection;
import kg.akyl.java.inventory.query.projections.SalesAnalyticsProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AnalyticsQueryHandler {
    @Autowired
    @Qualifier("readJdbcTemplate")
    private JdbcTemplate readJdbcTemplate;

    @Cacheable(value = "sales-analytics", key = "#fromDate + '_' + #toDate")
    public List<SalesAnalyticsProjection> getSalesAnalytics(LocalDateTime fromDate, LocalDateTime toDate) {
        String sql = """
            SELECT 
                p.category,
                COUNT(s.id) as total_sales,
                SUM(s.quantity) as total_quantity,
                SUM(s.total_amount) as total_revenue,
                AVG(s.total_amount) as avg_sale_amount,
                DATE(s.sale_date) as sale_date
            FROM sales s
            JOIN products p ON s.product_id = p.id
            WHERE s.sale_date BETWEEN ? AND ?
            AND s.status = 'CONFIRMED'
            GROUP BY p.category, DATE(s.sale_date)
            ORDER BY sale_date DESC, total_revenue DESC
            """;

        return readJdbcTemplate.query(sql,
                (rs, rowNum) -> new SalesAnalyticsProjection(
                        rs.getString("category"),
                        rs.getLong("total_sales"),
                        rs.getLong("total_quantity"),
                        rs.getBigDecimal("total_revenue"),
                        rs.getBigDecimal("avg_sale_amount"),
                        rs.getDate("sale_date").toLocalDate()
                ), fromDate, toDate);
    }

    @Cacheable(value = "inventory-analytics")
    public List<InventoryAnalyticsProjection> getInventoryAnalytics() {
        String sql = """
            WITH inventory_stats AS (
                SELECT 
                    p.category,
                    COUNT(*) as total_products,
                    SUM(p.quantity) as total_quantity,
                    SUM(p.reserved_quantity) as total_reserved,
                    SUM(p.quantity - p.reserved_quantity) as available_quantity,
                    SUM(p.quantity * p.price) as total_value,
                    AVG(p.price) as avg_price
                FROM products p
                WHERE p.status = 'ACTIVE'
                GROUP BY p.category
            ),
            sales_velocity AS (
                SELECT 
                    p.category,
                    COUNT(s.id) as sales_last_30_days,
                    SUM(s.quantity) as quantity_sold_30_days
                FROM products p
                LEFT JOIN sales s ON p.id = s.product_id 
                    AND s.sale_date >= CURRENT_DATE - INTERVAL '30 days'
                    AND s.status = 'CONFIRMED'
                WHERE p.status = 'ACTIVE'
                GROUP BY p.category
            )
            SELECT 
                i.category,
                i.total_products,
                i.total_quantity,
                i.total_reserved,
                i.available_quantity,
                i.total_value,
                i.avg_price,
                COALESCE(v.sales_last_30_days, 0) as sales_last_30_days,
                COALESCE(v.quantity_sold_30_days, 0) as quantity_sold_30_days,
                CASE 
                    WHEN v.quantity_sold_30_days > 0 
                    THEN i.available_quantity::float / (v.quantity_sold_30_days::float / 30)
                    ELSE NULL 
                END as days_of_inventory
            FROM inventory_stats i
            LEFT JOIN sales_velocity v ON i.category = v.category
            ORDER BY i.total_value DESC
            """;

        return readJdbcTemplate.query(sql,
                (rs, rowNum) -> new InventoryAnalyticsProjection(
                        rs.getString("category"),
                        rs.getInt("total_products"),
                        rs.getLong("total_quantity"),
                        rs.getLong("total_reserved"),
                        rs.getLong("available_quantity"),
                        rs.getBigDecimal("total_value"),
                        rs.getBigDecimal("avg_price"),
                        rs.getLong("sales_last_30_days"),
                        rs.getLong("quantity_sold_30_days"),
                        rs.getObject("days_of_inventory", Double.class)
                ));
    }

    public List<SalesAnalyticsProjection> getTopSellingProducts(int limit, LocalDateTime fromDate, LocalDateTime toDate) {
        String sql = """
            SELECT 
                p.name as category,
                COUNT(s.id) as total_sales,
                SUM(s.quantity) as total_quantity,
                SUM(s.total_amount) as total_revenue,
                AVG(s.total_amount) as avg_sale_amount,
                CURRENT_DATE as sale_date
            FROM sales s
            JOIN products p ON s.product_id = p.id
            WHERE s.sale_date BETWEEN ? AND ?
            AND s.status = 'CONFIRMED'
            GROUP BY p.id, p.name
            ORDER BY total_revenue DESC
            LIMIT ?
            """;

        return readJdbcTemplate.query(sql,
                (rs, rowNum) -> new SalesAnalyticsProjection(
                        rs.getString("category"),
                        rs.getLong("total_sales"),
                        rs.getLong("total_quantity"),
                        rs.getBigDecimal("total_revenue"),
                        rs.getBigDecimal("avg_sale_amount"),
                        rs.getDate("sale_date").toLocalDate()
                ), fromDate, toDate, limit);
    }
}
