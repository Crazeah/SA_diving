package com.dive.club.repository;

import com.dive.club.entity.Activity;
import com.dive.club.entity.Manager;
import com.dive.club.enums.ActivityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Activity Repository with custom query methods
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /**
     * Find all activities by status
     */
    List<Activity> findByStatus(ActivityStatus status);

    /**
     * Find all published activities (visible to public)
     */
    @Query("SELECT a FROM Activity a WHERE a.status = 'PUBLISHED' ORDER BY a.startTime DESC")
    List<Activity> findAllPublished();

    /**
     * Find activities by category and status
     */
    List<Activity> findByCategoryAndStatus(String category, ActivityStatus status);

    /**
     * Find activities created by a specific manager
     */
    List<Activity> findByCreator(Manager creator);

    /**
     * Find activities created by a specific manager with specific status
     */
    List<Activity> findByCreatorAndStatus(Manager creator, ActivityStatus status);

    /**
     * Find all activities pending review
     */
    @Query("SELECT a FROM Activity a WHERE a.status = 'PENDING_REVIEW' ORDER BY a.createdAt ASC")
    List<Activity> findAllPendingReview();

    /**
     * Find published activities that have ended (for cron job)
     */
    @Query("SELECT a FROM Activity a WHERE a.status = 'PUBLISHED' AND a.endTime < :currentTime")
    List<Activity> findPublishedActivitiesPastEndTime(@Param("currentTime") LocalDateTime currentTime);

    /**
     * Search activities by keyword in title or description
     */
    @Query("SELECT a FROM Activity a WHERE a.status = 'PUBLISHED' AND " +
            "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Activity> searchPublishedActivities(@Param("keyword") String keyword);

    /**
     * Count activities by status
     */
    long countByStatus(ActivityStatus status);

    /**
     * Find activities by multiple statuses
     */
    List<Activity> findByStatusIn(List<ActivityStatus> statuses);
}
