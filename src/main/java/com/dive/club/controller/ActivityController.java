package com.dive.club.controller;

import com.dive.club.dto.*;
import com.dive.club.entity.Activity;
import com.dive.club.entity.Manager;
import com.dive.club.enums.ActivityStatus;
import com.dive.club.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Activity REST API Controller
 * Handles all activity-related HTTP requests
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
@Slf4j
public class ActivityController {

    private final ActivityService activityService;

    /**
     * UC-A05: Create Activity
     * POST /api/activities
     * Access: Manager, SuperManager
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> createActivity(
            @Valid @RequestBody ActivityCreateDTO dto,
            @AuthenticationPrincipal Manager manager) {

        log.info("POST /api/activities - Creating activity by {}", manager.getEmail());

        Activity activity = activityService.createActivity(dto, manager);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "活動建立成功，狀態為草稿");
        response.put("data", ActivityResponseDTO.fromEntity(activity));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Submit activity for review
     * POST /api/activities/{id}/submit
     * Access: Manager (owner only)
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> submitForReview(
            @PathVariable Long id,
            @AuthenticationPrincipal Manager manager) {

        log.info("POST /api/activities/{}/submit - Submitted by {}", id, manager.getEmail());

        Activity activity = activityService.submitForReview(id, manager);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "提交成功，等待管理員審核");
        response.put("data", ActivityResponseDTO.fromEntity(activity));

        return ResponseEntity.ok(response);
    }

    /**
     * UC-A09: Audit Activity (Approve/Reject)
     * POST /api/activities/{id}/audit
     * Access: SuperManager only
     */
    @PostMapping("/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> auditActivity(
            @PathVariable Long id,
            @Valid @RequestBody AuditDecisionDTO decision) {

        log.info("POST /api/activities/{}/audit - Decision: {}", id, decision.getAction());

        Activity activity = activityService.auditActivity(id, decision);

        String message = decision.getAction() == AuditDecisionDTO.AuditAction.APPROVE
                ? "活動審核通過，已發布"
                : "活動已退回，需要修正";

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", ActivityResponseDTO.fromEntity(activity));

        return ResponseEntity.ok(response);
    }

    /**
     * Update Activity
     * PUT /api/activities/{id}
     * Access: Manager (owner only)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody ActivityUpdateDTO dto,
            @AuthenticationPrincipal Manager manager) {

        log.info("PUT /api/activities/{} - Updated by {}", id, manager.getEmail());

        Activity activity = activityService.updateActivity(id, dto, manager);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "活動更新成功");
        response.put("data", ActivityResponseDTO.fromEntity(activity));

        return ResponseEntity.ok(response);
    }

    /**
     * Delete Activity
     * DELETE /api/activities/{id}
     * Access: Manager (owner only)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal Manager manager) {

        log.info("DELETE /api/activities/{} - Deleted by {}", id, manager.getEmail());

        activityService.deleteActivity(id, manager);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "活動刪除成功");

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel Activity
     * POST /api/activities/{id}/cancel
     * Access: Manager (owner only)
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> cancelActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal Manager manager) {

        log.info("POST /api/activities/{}/cancel - Cancelled by {}", id, manager.getEmail());

        Activity activity = activityService.cancelActivity(id, manager);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "活動已取消");
        response.put("data", ActivityResponseDTO.fromEntity(activity));

        return ResponseEntity.ok(response);
    }

    /**
     * Get Activity by ID
     * GET /api/activities/{id}
     * Access: All authenticated users
     */
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getActivity(@PathVariable Long id) {
        log.info("GET /api/activities/{}", id);

        Activity activity = activityService.getActivityById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", ActivityResponseDTO.fromEntity(activity));

        return ResponseEntity.ok(response);
    }

    /**
     * Get All Published Activities
     * GET /api/activities
     * Access: Public (including guests)
     */
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getAllPublishedActivities() {
        log.info("GET /api/activities - Getting all published activities");

        List<Activity> activities = activityService.getAllPublishedActivities();
        List<ActivityResponseDTO> activityDTOs = activities.stream()
                .map(ActivityResponseDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", activityDTOs.size());
        response.put("data", activityDTOs);

        return ResponseEntity.ok(response);
    }

    /**
     * Get Activities Pending Review
     * GET /api/activities/pending
     * Access: SuperManager only
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getPendingActivities() {
        log.info("GET /api/activities/pending");

        List<Activity> activities = activityService.getAllPendingReviewActivities();
        List<ActivityResponseDTO> activityDTOs = activities.stream()
                .map(ActivityResponseDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", activityDTOs.size());
        response.put("data", activityDTOs);

        return ResponseEntity.ok(response);
    }

    /**
     * Get My Activities (created by current manager)
     * GET /api/activities/my
     * Access: Manager, SuperManager
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getMyActivities(
            @AuthenticationPrincipal Manager manager) {

        log.info("GET /api/activities/my - User: {}", manager.getEmail());

        List<Activity> activities = activityService.getActivitiesByCreator(manager);
        List<ActivityResponseDTO> activityDTOs = activities.stream()
                .map(ActivityResponseDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", activityDTOs.size());
        response.put("data", activityDTOs);

        return ResponseEntity.ok(response);
    }

    /**
     * Search Activities
     * GET /api/activities/search?keyword={keyword}
     * Access: Public
     */
    @GetMapping("/search")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> searchActivities(
            @RequestParam String keyword) {

        log.info("GET /api/activities/search?keyword={}", keyword);

        List<Activity> activities = activityService.searchPublishedActivities(keyword);
        List<ActivityResponseDTO> activityDTOs = activities.stream()
                .map(ActivityResponseDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", activityDTOs.size());
        response.put("data", activityDTOs);

        return ResponseEntity.ok(response);
    }

    /**
     * Filter Activities by Category
     * GET /api/activities/category/{category}
     * Access: Public
     */
    @GetMapping("/category/{category}")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getActivitiesByCategory(
            @PathVariable String category) {

        log.info("GET /api/activities/category/{}", category);

        List<Activity> activities = activityService.getPublishedActivitiesByCategory(category);
        List<ActivityResponseDTO> activityDTOs = activities.stream()
                .map(ActivityResponseDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", activityDTOs.size());
        response.put("data", activityDTOs);

        return ResponseEntity.ok(response);
    }

    /**
     * Get Activities by Status
     * GET /api/activities/status/{status}
     * Access: Manager, SuperManager
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> getActivitiesByStatus(
            @PathVariable ActivityStatus status) {

        log.info("GET /api/activities/status/{}", status);

        List<Activity> activities = activityService.getActivitiesByStatus(status);
        List<ActivityResponseDTO> activityDTOs = activities.stream()
                .map(ActivityResponseDTO::fromEntity)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", activityDTOs.size());
        response.put("data", activityDTOs);

        return ResponseEntity.ok(response);
    }
}
