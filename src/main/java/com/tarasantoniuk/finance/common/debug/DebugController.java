package com.tarasantoniuk.finance.common.debug;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@Tag(name = "Debug", description = "Debug and performance monitoring endpoints")
public class DebugController {

    @PersistenceContext
    private EntityManager entityManager;

    @GetMapping("/hibernate-stats")
    @Operation(summary = "Get Hibernate statistics",
            description = "Returns Hibernate performance statistics including query counts")
    public ResponseEntity<Map<String, Object>> getHibernateStats() {
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();

        Map<String, Object> result = new HashMap<>();
        result.put("statisticsEnabled", stats.isStatisticsEnabled());
        result.put("queriesExecuted", stats.getQueryExecutionCount());
        result.put("entitiesLoaded", stats.getEntityLoadCount());
        result.put("entitiesFetched", stats.getEntityFetchCount());
        result.put("collectionsLoaded", stats.getCollectionLoadCount());
        result.put("collectionsFetched", stats.getCollectionFetchCount());
        result.put("connectionsObtained", stats.getConnectCount());
        result.put("prepareStatementCount", stats.getPrepareStatementCount());
        result.put("queryExecutionMaxTime", stats.getQueryExecutionMaxTime() + " ms");
        result.put("sessionOpenCount", stats.getSessionOpenCount());
        result.put("sessionCloseCount", stats.getSessionCloseCount());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/hibernate-stats/clear")
    @Operation(summary = "Clear Hibernate statistics",
            description = "Resets all Hibernate statistics counters")
    public ResponseEntity<Map<String, String>> clearStats() {
        SessionFactory sessionFactory = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class);
        Statistics stats = sessionFactory.getStatistics();
        stats.clear();

        Map<String, String> result = new HashMap<>();
        result.put("message", "Hibernate statistics cleared successfully");
        result.put("timestamp", java.time.LocalDateTime.now().toString());

        return ResponseEntity.ok(result);
    }
}