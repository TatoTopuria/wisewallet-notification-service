package com.wisewallet.notification.infrastructure.job;

import com.wisewallet.notification.domain.repository.ProcessedEventRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Purges processed_events rows older than the configured retention period.
 * Runs daily at 2 AM to keep the table size manageable.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessedEventCleanupJob {

    private final ProcessedEventRepositoryPort processedEventRepository;

    @Value("${wisewallet.notification.cleanup.processed-events-retention-days:7}")
    private int retentionDays;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldEvents() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        int deleted = processedEventRepository.deleteByProcessedAtBefore(cutoff);
        log.info("ProcessedEvent cleanup: deleted {} rows older than {} days", deleted, retentionDays);
    }
}
