package com.dive.club.scheduler;

import com.dive.club.service.ActivityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled Task for Activity Status Management
 * Automatically marks published activities as ENDED when their end time has
 * passed
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ActivityStatusScheduler {

    private final ActivityService activityService;

    /**
     * Run every hour to check and mark ended activities
     * Cron expression: "0 0 * * * *" = every hour at the top of the hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void markEndedActivities() {
        log.info("Running scheduled task: Mark ended activities");

        try {
            activityService.markEndedActivities();
            log.info("Scheduled task completed: Mark ended activities");
        } catch (Exception e) {
            log.error("Error in scheduled task: Mark ended activities", e);
        }
    }

    /**
     * Alternative: Run every 30 minutes
     * Uncomment to use instead of hourly
     */
    // @Scheduled(fixedRate = 1800000) // 30 minutes in milliseconds
    // public void markEndedActivitiesFrequent() {
    // log.info("Running scheduled task (30 min): Mark ended activities");
    // activityService.markEndedActivities();
    // }
}
