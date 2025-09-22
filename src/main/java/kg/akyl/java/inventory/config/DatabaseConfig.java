package kg.akyl.java.inventory.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "kg.akyl.java.inventory.infra.repositories")
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    @Primary
    public DataSource writeDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);

        // High-performance tuning for write operations
        config.setMaximumPoolSize(50);
        config.setMinimumIdle(10);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        // PostgreSQL specific optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");

        return new HikariDataSource(config);
    }

    @Bean
    public DataSource readDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl.replace("5432", "5432")); // Read replica port
        config.setUsername(username);
        config.setPassword(password);

        // Optimized for read operations
        config.setMaximumPoolSize(30);
        config.setMinimumIdle(5);
        config.setReadOnly(true);

        return new HikariDataSource(config);
    }

    @Bean
    public JdbcTemplate writeJdbcTemplate() {
        return new JdbcTemplate(writeDataSource());
    }

    @Bean
    public JdbcTemplate readJdbcTemplate() {
        return new JdbcTemplate(readDataSource());
    }
}
