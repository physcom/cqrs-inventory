package kg.akyl.java.inventory.infra.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import kg.akyl.java.inventory.domain.InventoryEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventStore {

    @Autowired
    @Qualifier("writeJdbcTemplate")
    private JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper;

    public void saveEvent(InventoryEvent event) {
        String sql = """
            INSERT INTO event_store (id, event_type, aggregate_id, event_data, timestamp, version)
            VALUES (?, ?, ?, ?::jsonb, ?, ?)
            """;

        try {
            String eventDataJson = objectMapper.writeValueAsString(event.getEventData());
            jdbcTemplate.update(sql,
                    event.getId(),
                    event.getEventType(),
                    event.getAggregateId(),
                    eventDataJson,
                    Timestamp.valueOf(event.getTimestamp()),
                    event.getVersion()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to save event", e);
        }
    }

    public List<InventoryEvent> getEventsByAggregateId(String aggregateId) {
        String sql = """
            SELECT id, event_type, aggregate_id, event_data, timestamp, version
            FROM event_store\s
            WHERE aggregate_id = ?
            ORDER BY timestamp ASC
           \s""";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    InventoryEvent event = new InventoryEvent();
                    event.setId(rs.getString("id"));
                    event.setEventType(rs.getString("event_type"));
                    event.setAggregateId(rs.getString("aggregate_id"));
                    event.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    event.setVersion(rs.getLong("version"));

                    try {
                        String eventDataJson = rs.getString("event_data");
                        Object eventData = objectMapper.readValue(eventDataJson, Object.class);
                        event.setEventData(eventData);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize event data", e);
                    }

                    return event;
                }, aggregateId);
    }

    public List<InventoryEvent> getEventsByType(String eventType, LocalDateTime fromDate, LocalDateTime toDate) {
        String sql = """
            SELECT id, event_type, aggregate_id, event_data, timestamp, version
            FROM event_store\s
            WHERE event_type = ? AND timestamp BETWEEN ? AND ?
            ORDER BY timestamp
           \s""";

        return jdbcTemplate.query(sql,
                (rs, rowNum) -> {
                    InventoryEvent event = new InventoryEvent();
                    event.setId(rs.getString("id"));
                    event.setEventType(rs.getString("event_type"));
                    event.setAggregateId(rs.getString("aggregate_id"));
                    event.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    event.setVersion(rs.getLong("version"));

                    try {
                        String eventDataJson = rs.getString("event_data");
                        Object eventData = objectMapper.readValue(eventDataJson, Object.class);
                        event.setEventData(eventData);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to deserialize event data", e);
                    }

                    return event;
                }, eventType, Timestamp.valueOf(fromDate), Timestamp.valueOf(toDate));
    }
}
