package kg.gns.java.inventorysystem.infra.interceptors;

import io.micrometer.core.instrument.Timer;
import kg.gns.java.inventorysystem.infra.metrics.InventoryMetrics;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PerformanceMonitoringAspect {
    @Autowired
    private InventoryMetrics inventoryMetrics;

    @Around("execution(* kg.gns.java.inventorysystem.query.handlers.ProductQueryHandler.*(..))")
    public Object monitorProductQueries(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("Monitoring product queries");
        Timer.Sample sample = inventoryMetrics.startProductQueryTimer();
        try {
            return joinPoint.proceed();
        } finally {
            inventoryMetrics.recordProductQueryTime(sample);
        }
    }

    @Around("execution(* kg.gns.java.inventorysystem.command.handlers.SaleCommandHandler.*(..))")
    public Object monitorSaleProcessing(ProceedingJoinPoint joinPoint) throws Throwable {
        Timer.Sample sample = inventoryMetrics.startSaleProcessingTimer();
        try {
            Object result = joinPoint.proceed();
            inventoryMetrics.incrementSalesProcessed();
            return result;
        } finally {
            inventoryMetrics.recordSaleProcessingTime(sample);
        }
    }

    @Around("execution(* kg.gns.java.inventorysystem.command.handlers.ProductCommandHandler.handle(..))")
    public Object monitorProductCommands(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object result = joinPoint.proceed();
        log.info("Monitoring product queries");
        // Increment appropriate counters based on command type
        if (joinPoint.getArgs().length > 0) {
            String commandType = joinPoint.getArgs()[0].getClass().getSimpleName();
            if (commandType.contains("CreateProduct")) {
                inventoryMetrics.incrementProductsCreated();
            } else if (commandType.contains("UpdateProductQuantity")) {
                inventoryMetrics.incrementInventoryUpdates();
            }
        }

        return result;
    }
}
