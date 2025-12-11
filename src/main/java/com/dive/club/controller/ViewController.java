package com.dive.club.controller;

import com.dive.club.dto.ActivityCreateDTO;
import com.dive.club.dto.ActivityUpdateDTO;
import com.dive.club.dto.AuditDecisionDTO;
import com.dive.club.entity.Activity;
import com.dive.club.entity.Manager;
import com.dive.club.entity.User;
import com.dive.club.enums.ActivityStatus;
import com.dive.club.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * View Controller for Thymeleaf SSR pages
 * Handles all view rendering with server-side data population
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ViewController {

    private final ActivityService activityService;

    @GetMapping("/")
    @Transactional(readOnly = true)
    public String index(Model model, @AuthenticationPrincipal User currentUser) {
        List<Activity> publishedActivities = activityService.getAllPublishedActivities();
        List<Activity> pendingActivities = activityService.getActivitiesByStatus(ActivityStatus.PENDING_REVIEW);

        // Get recent activities (up to 3 for homepage display)
        List<Activity> recentActivities = publishedActivities.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(3)
                .toList();

        model.addAttribute("user", currentUser);
        model.addAttribute("totalActivities", publishedActivities.size());
        model.addAttribute("pendingCount", pendingActivities.size());
        model.addAttribute("recentActivities", recentActivities);
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ==================== Activity List (SSR) ====================

    @GetMapping("/activity/list")
    @Transactional(readOnly = true)
    public String activityList(
            Model model,
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {

        List<Activity> activities;

        if (keyword != null && !keyword.trim().isEmpty()) {
            activities = activityService.searchPublishedActivities(keyword);
        } else if (category != null && !category.trim().isEmpty()) {
            activities = activityService.getPublishedActivitiesByCategory(category);
        } else {
            activities = activityService.getAllPublishedActivities();
        }

        model.addAttribute("activities", activities);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        return "activity_list";
    }

    // ==================== Activity Create (SSR) ====================

    @GetMapping("/activity/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String createActivityForm(Model model, @AuthenticationPrincipal User currentUser) {
        model.addAttribute("activityForm", new ActivityCreateDTO());
        model.addAttribute("currentUser", currentUser);
        return "activity_create";
    }

    @PostMapping("/activity/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String createActivity(
            @Valid @ModelAttribute("activityForm") ActivityCreateDTO dto,
            BindingResult bindingResult,
            @RequestParam(required = false, defaultValue = "false") boolean submitForReview,
            @AuthenticationPrincipal Manager manager,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Creating activity: {} by {}", dto.getTitle(), manager.getEmail());

        if (bindingResult.hasErrors()) {
            model.addAttribute("currentUser", manager);
            return "activity_create";
        }

        try {
            Activity activity = activityService.createActivity(dto, manager);

            if (submitForReview) {
                activityService.submitForReview(activity.getActivityId(), manager);
                redirectAttributes.addFlashAttribute("successMessage", "活動已建立並送交審核");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "活動草稿已儲存");
            }

            return "redirect:/activity/my-list";
        } catch (Exception e) {
            log.error("Error creating activity", e);
            model.addAttribute("errorMessage", "建立活動失敗: " + e.getMessage());
            model.addAttribute("currentUser", manager);
            return "activity_create";
        }
    }

    // ==================== Activity Edit (SSR) ====================

    @GetMapping("/activity/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public String editActivityForm(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal Manager manager,
            RedirectAttributes redirectAttributes) {

        try {
            Activity activity = activityService.getActivityById(id);

            // Check ownership
            if (!activity.getCreator().getId().equals(manager.getId())) {
                redirectAttributes.addFlashAttribute("errorMessage", "您沒有權限編輯此活動");
                return "redirect:/activity/my-list";
            }

            model.addAttribute("activity", activity);
            model.addAttribute("currentUser", manager);
            return "activity_edit";
        } catch (Exception e) {
            log.error("Error loading activity for edit", e);
            redirectAttributes.addFlashAttribute("errorMessage", "載入活動失敗: " + e.getMessage());
            return "redirect:/activity/my-list";
        }
    }

    @PostMapping("/activity/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String updateActivity(
            @PathVariable Long id,
            @Valid @ModelAttribute ActivityUpdateDTO dto,
            BindingResult bindingResult,
            @RequestParam(required = false, defaultValue = "false") boolean submitForReview,
            @AuthenticationPrincipal Manager manager,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.info("Updating activity {} by {}", id, manager.getEmail());

        if (bindingResult.hasErrors()) {
            try {
                Activity activity = activityService.getActivityById(id);
                model.addAttribute("activity", activity);
                model.addAttribute("currentUser", manager);
            } catch (Exception e) {
                // If we can't load the activity, redirect
                redirectAttributes.addFlashAttribute("errorMessage", "載入活動失敗");
                return "redirect:/activity/my-list";
            }
            return "activity_edit";
        }

        try {
            Activity activity = activityService.updateActivity(id, dto, manager);

            if (submitForReview) {
                activityService.submitForReview(activity.getActivityId(), manager);
                redirectAttributes.addFlashAttribute("successMessage", "活動已更新並重新送交審核");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "活動已更新");
            }

            return "redirect:/activity/my-list";
        } catch (Exception e) {
            log.error("Error updating activity", e);
            redirectAttributes.addFlashAttribute("errorMessage", "更新活動失敗: " + e.getMessage());
            return "redirect:/activity/edit/" + id;
        }
    }

    // ==================== My Activity List (SSR) ====================

    @GetMapping("/activity/my-list")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public String myActivityList(Model model, @AuthenticationPrincipal Manager manager) {
        List<Activity> myActivities = activityService.getActivitiesByCreator(manager);
        model.addAttribute("activities", myActivities);
        model.addAttribute("currentUser", manager);
        return "activity_my_list";
    }

    // ==================== Activity Detail (SSR) ====================

    @GetMapping("/activity/{id}")
    @Transactional(readOnly = true)
    public String activityDetail(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal User currentUser) {

        Activity activity = activityService.getActivityById(id);
        model.addAttribute("activity", activity);
        model.addAttribute("currentUser", currentUser);
        return "activity_detail";
    }

    // ==================== Activity Delete (SSR) ====================

    @PostMapping("/activity/delete/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String deleteActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal Manager manager,
            RedirectAttributes redirectAttributes) {

        log.info("Deleting activity {} by {}", id, manager.getEmail());

        try {
            activityService.deleteActivity(id, manager);
            redirectAttributes.addFlashAttribute("successMessage", "活動已刪除");
        } catch (Exception e) {
            log.error("Error deleting activity", e);
            redirectAttributes.addFlashAttribute("errorMessage", "刪除活動失敗: " + e.getMessage());
        }

        return "redirect:/activity/my-list";
    }

    // ==================== Activity Submit for Review (SSR) ====================

    @PostMapping("/activity/submit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String submitForReview(
            @PathVariable Long id,
            @AuthenticationPrincipal Manager manager,
            RedirectAttributes redirectAttributes) {

        log.info("Submitting activity {} for review by {}", id, manager.getEmail());

        try {
            activityService.submitForReview(id, manager);
            redirectAttributes.addFlashAttribute("successMessage", "活動已送交審核");
        } catch (Exception e) {
            log.error("Error submitting activity for review", e);
            redirectAttributes.addFlashAttribute("errorMessage", "送審失敗: " + e.getMessage());
        }

        return "redirect:/activity/my-list";
    }

    // ==================== Activity Cancel (SSR) ====================

    @PostMapping("/activity/cancel/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String cancelActivity(
            @PathVariable Long id,
            @AuthenticationPrincipal Manager manager,
            RedirectAttributes redirectAttributes) {

        log.info("Cancelling activity {} by {}", id, manager.getEmail());

        try {
            activityService.cancelActivity(id, manager);
            redirectAttributes.addFlashAttribute("successMessage", "活動已取消");
        } catch (Exception e) {
            log.error("Error cancelling activity", e);
            redirectAttributes.addFlashAttribute("errorMessage", "取消活動失敗: " + e.getMessage());
        }

        return "redirect:/activity/my-list";
    }

    // ==================== Audit List (SSR) ====================

    @GetMapping("/activity/audit/list")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String auditList(
            Model model,
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {

        List<Activity> pendingActivities = activityService.getActivitiesByStatus(ActivityStatus.PENDING_REVIEW);

        // Filter by keyword if provided
        if (keyword != null && !keyword.trim().isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            pendingActivities = pendingActivities.stream()
                    .filter(a -> a.getTitle().toLowerCase().contains(lowerKeyword) ||
                            a.getCreator().getName().toLowerCase().contains(lowerKeyword))
                    .toList();
        }

        // Filter by category if provided
        if (category != null && !category.trim().isEmpty()) {
            pendingActivities = pendingActivities.stream()
                    .filter(a -> a.getCategory().equalsIgnoreCase(category))
                    .toList();
        }

        // Sort by creation time
        if ("oldest".equals(sortBy)) {
            pendingActivities = pendingActivities.stream()
                    .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                    .toList();
        } else {
            pendingActivities = pendingActivities.stream()
                    .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                    .toList();
        }

        model.addAttribute("pendingActivities", pendingActivities);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("pendingCount", pendingActivities.size());
        return "activity_audit_list";
    }

    // ==================== Audit Detail (SSR) ====================

    @GetMapping("/activity/audit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String auditDetail(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal User currentUser) {

        Activity activity = activityService.getActivityById(id);
        model.addAttribute("activity", activity);
        model.addAttribute("currentUser", currentUser);
        return "activity_audit_detail";
    }

    // ==================== Audit Actions (SSR) ====================

    @PostMapping("/activity/audit/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public String approveActivity(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        log.info("Approving activity {}", id);

        try {
            AuditDecisionDTO decision = new AuditDecisionDTO();
            decision.setAction(AuditDecisionDTO.AuditAction.APPROVE);

            activityService.auditActivity(id, decision);
            redirectAttributes.addFlashAttribute("successMessage", "活動已核准發佈");
        } catch (Exception e) {
            log.error("Error approving activity", e);
            redirectAttributes.addFlashAttribute("errorMessage", "核准失敗: " + e.getMessage());
        }

        return "redirect:/activity/audit/list";
    }

    @PostMapping("/activity/audit/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public String rejectActivity(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes) {

        log.info("Rejecting activity {} with reason: {}", id, reason);

        try {
            AuditDecisionDTO decision = new AuditDecisionDTO();
            decision.setAction(AuditDecisionDTO.AuditAction.REJECT);
            decision.setReason(reason);

            activityService.auditActivity(id, decision);
            redirectAttributes.addFlashAttribute("successMessage", "活動已退回修改");
        } catch (Exception e) {
            log.error("Error rejecting activity", e);
            redirectAttributes.addFlashAttribute("errorMessage", "退回失敗: " + e.getMessage());
        }

        return "redirect:/activity/audit/list";
    }

    // ==================== Backward Compatibility Routes ====================

    @GetMapping("/activities/create")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public String createActivityOld(Model model, @AuthenticationPrincipal User currentUser) {
        return createActivityForm(model, currentUser);
    }

    @GetMapping("/activities/edit/{id}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    @Transactional(readOnly = true)
    public String editActivityOld(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal Manager manager,
            RedirectAttributes redirectAttributes) {
        return editActivityForm(id, model, manager, redirectAttributes);
    }

    @GetMapping("/activities/audit")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String auditListOld(
            Model model,
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "newest") String sortBy) {
        return auditList(model, currentUser, keyword, category, sortBy);
    }

    @GetMapping("/activities/audit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String auditDetailOld(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal User currentUser) {
        return auditDetail(id, model, currentUser);
    }
}
