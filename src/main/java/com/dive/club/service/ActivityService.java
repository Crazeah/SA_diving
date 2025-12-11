package com.dive.club.service;

import com.dive.club.dto.ActivityCreateDTO;
import com.dive.club.dto.ActivityUpdateDTO;
import com.dive.club.dto.AuditDecisionDTO;
import com.dive.club.entity.Activity;
import com.dive.club.entity.Manager;
import com.dive.club.enums.ActivityStatus;
import com.dive.club.exception.ActivityNotFoundException;
import com.dive.club.exception.UnauthorizedException;
import com.dive.club.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Activity Service - Business Logic Layer
 * Implements state machine and activity management logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final EmailService emailService;

    /**
     * Create a new activity (UC-A05)
     * Initial status: DRAFTING
     */
    public Activity createActivity(ActivityCreateDTO dto, Manager creator) {
        log.info("Creating new activity '{}' by manager {}", dto.getTitle(), creator.getEmail());

        Activity activity = new Activity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setCategory(dto.getCategory());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setLocation(dto.getLocation());
        activity.setMaxParticipants(dto.getMaxParticipants());
        activity.setCost(dto.getCost());
        activity.setQualifications(dto.getQualifications());
        activity.setImageUrl(dto.getImageUrl());
        activity.setStatus(ActivityStatus.DRAFTING);
        activity.setCreator(creator);

        Activity savedActivity = activityRepository.save(activity);
        log.info("Activity created with ID: {}", savedActivity.getActivityId());

        return savedActivity;
    }

    /**
     * Submit activity for review
     * Transition: DRAFTING/NEEDS_REVISION -> PENDING_REVIEW
     */
    public Activity submitForReview(Long activityId, Manager manager) {
        log.info("Submitting activity {} for review", activityId);

        Activity activity = getActivityById(activityId);

        // Verify ownership
        if (!activity.getCreator().getId().equals(manager.getId())) {
            throw new UnauthorizedException("您沒有權限提交此活動");
        }

        activity.submitForReview();
        Activity savedActivity = activityRepository.save(activity);

        // Send notifications
        emailService.sendSubmissionNotification(savedActivity);
        emailService.notifyAdminNewSubmission(savedActivity);

        log.info("Activity {} submitted for review", activityId);
        return savedActivity;
    }

    /**
     * Audit activity (UC-A09)
     * Approve: PENDING_REVIEW -> PUBLISHED
     * Reject: PENDING_REVIEW -> NEEDS_REVISION
     */
    public Activity auditActivity(Long activityId, AuditDecisionDTO decision) {
        log.info("Auditing activity {} with decision {}", activityId, decision.getAction());

        Activity activity = getActivityById(activityId);

        if (!decision.isValid()) {
            throw new IllegalArgumentException("退回活動時必須提供原因");
        }

        if (decision.getAction() == AuditDecisionDTO.AuditAction.APPROVE) {
            activity.approve();
            activityRepository.save(activity);
            emailService.sendApprovalNotification(activity);
            log.info("Activity {} approved", activityId);
        } else {
            activity.reject(decision.getReason());
            activityRepository.save(activity);
            emailService.sendRejectionNotification(activity, decision.getReason());
            log.info("Activity {} rejected with reason: {}", activityId, decision.getReason());
        }

        return activity;
    }

    /**
     * Update activity
     * Only allowed for DRAFTING or NEEDS_REVISION status
     * If updating PUBLISHED activity with major changes, status goes back to
     * PENDING_REVIEW
     */
    public Activity updateActivity(Long activityId, ActivityUpdateDTO dto, Manager manager) {
        log.info("Updating activity {} by manager {}", activityId, manager.getEmail());

        Activity activity = getActivityById(activityId);

        // Verify ownership
        if (!activity.getCreator().getId().equals(manager.getId())) {
            throw new UnauthorizedException("您沒有權限修改此活動");
        }

        // Check if activity can be edited
        if (!activity.canBeEdited() && activity.getStatus() != ActivityStatus.PUBLISHED) {
            throw new IllegalStateException("此活動目前狀態無法編輯");
        }

        // Detect major changes for published activities
        boolean majorChange = false;
        if (activity.getStatus() == ActivityStatus.PUBLISHED) {
            majorChange = isMajorChange(activity, dto);
        }

        // Update fields
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setCategory(dto.getCategory());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setLocation(dto.getLocation());
        activity.setMaxParticipants(dto.getMaxParticipants());
        activity.setCost(dto.getCost());
        activity.setQualifications(dto.getQualifications());
        activity.setImageUrl(dto.getImageUrl());

        // If major change to published activity, revert to pending review
        if (majorChange) {
            activity.setStatus(ActivityStatus.PENDING_REVIEW);
            log.info("Activity {} major change detected, reverting to PENDING_REVIEW", activityId);
        }

        Activity savedActivity = activityRepository.save(activity);
        log.info("Activity {} updated", activityId);

        return savedActivity;
    }

    /**
     * Delete activity
     * Only allowed by creator for non-published activities
     */
    public void deleteActivity(Long activityId, Manager manager) {
        log.info("Deleting activity {} by manager {}", activityId, manager.getEmail());

        Activity activity = getActivityById(activityId);

        // Verify ownership
        if (!activity.getCreator().getId().equals(manager.getId())) {
            throw new UnauthorizedException("您沒有權限刪除此活動");
        }

        // Cannot delete published or ended activities
        if (activity.getStatus() == ActivityStatus.PUBLISHED ||
                activity.getStatus() == ActivityStatus.ENDED) {
            throw new IllegalStateException("已發布或已結束的活動無法刪除，請改為取消");
        }

        activityRepository.delete(activity);
        log.info("Activity {} deleted", activityId);
    }

    /**
     * Cancel activity
     */
    public Activity cancelActivity(Long activityId, Manager manager) {
        log.info("Cancelling activity {} by manager {}", activityId, manager.getEmail());

        Activity activity = getActivityById(activityId);

        // Verify ownership
        if (!activity.getCreator().getId().equals(manager.getId())) {
            throw new UnauthorizedException("您沒有權限取消此活動");
        }

        activity.cancel();
        Activity savedActivity = activityRepository.save(activity);
        log.info("Activity {} cancelled", activityId);

        return savedActivity;
    }

    /**
     * Get activity by ID
     */
    public Activity getActivityById(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException("找不到活動 ID: " + activityId));
    }

    /**
     * Get all published activities (visible to public)
     */
    public List<Activity> getAllPublishedActivities() {
        return activityRepository.findAllPublished();
    }

    /**
     * Get all activities pending review
     */
    public List<Activity> getAllPendingReviewActivities() {
        return activityRepository.findAllPendingReview();
    }

    /**
     * Get activities created by a manager
     */
    public List<Activity> getActivitiesByCreator(Manager manager) {
        return activityRepository.findByCreator(manager);
    }

    /**
     * Get activities by status
     */
    public List<Activity> getActivitiesByStatus(ActivityStatus status) {
        return activityRepository.findByStatus(status);
    }

    /**
     * Search published activities by keyword
     */
    public List<Activity> searchPublishedActivities(String keyword) {
        return activityRepository.searchPublishedActivities(keyword);
    }

    /**
     * Filter published activities by category
     */
    public List<Activity> getPublishedActivitiesByCategory(String category) {
        return activityRepository.findByCategoryAndStatus(category, ActivityStatus.PUBLISHED);
    }

    /**
     * Mark ended activities (called by scheduled task)
     */
    public void markEndedActivities() {
        List<Activity> endedActivities = activityRepository.findPublishedActivitiesPastEndTime(
                java.time.LocalDateTime.now());

        for (Activity activity : endedActivities) {
            activity.markAsEnded();
            activityRepository.save(activity);
            log.info("Activity {} marked as ENDED", activity.getActivityId());
        }

        if (!endedActivities.isEmpty()) {
            log.info("Marked {} activities as ENDED", endedActivities.size());
        }
    }

    /**
     * Detect if update contains major changes for published activity
     */
    private boolean isMajorChange(Activity original, ActivityUpdateDTO updated) {
        return !original.getTitle().equals(updated.getTitle()) ||
                !original.getStartTime().equals(updated.getStartTime()) ||
                !original.getEndTime().equals(updated.getEndTime()) ||
                !original.getLocation().equals(updated.getLocation()) ||
                !original.getMaxParticipants().equals(updated.getMaxParticipants());
    }
}
