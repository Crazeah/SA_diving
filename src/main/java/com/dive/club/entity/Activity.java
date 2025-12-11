package com.dive.club.entity;

import com.dive.club.enums.ActivityStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Activity Entity - 活動實體
 * Core entity for the activity management system
 */
@Entity
@Table(name = "activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long activityId;

    @Column(nullable = false, length = 200)
    @NotBlank(message = "活動標題不能為空")
    @Size(min = 3, max = 200, message = "活動標題長度必須在3-200字元之間")
    private String title;

    @Column(columnDefinition = "TEXT")
    @NotBlank(message = "活動描述不能為空")
    private String description;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "活動類別不能為空")
    private String category; // 活動類別 (e.g., "潛水訓練", "海洋保育", "社交活動")

    @Column(nullable = false)
    @NotNull(message = "開始時間不能為空")
    @Future(message = "開始時間必須是未來時間")
    private LocalDateTime startTime;

    @Column(nullable = false)
    @NotNull(message = "結束時間不能為空")
    private LocalDateTime endTime;

    @Column(nullable = false, length = 300)
    @NotBlank(message = "活動地點不能為空")
    private String location;

    @Column(nullable = false)
    @NotNull(message = "人數上限不能為空")
    @Min(value = 1, message = "人數上限至少為1人")
    @Max(value = 1000, message = "人數上限不能超過1000人")
    private Integer maxParticipants;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "費用不能為空")
    @DecimalMin(value = "0.0", message = "費用不能為負數")
    private BigDecimal cost;

    @Column(columnDefinition = "TEXT")
    private String qualifications; // 參加資格要求

    @Column(length = 500)
    private String imageUrl; // 活動圖片URL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityStatus status = ActivityStatus.DRAFTING;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason; // 退回原因

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private Manager creator; // 活動建立者

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Business Logic Methods

    /**
     * Validate date logic: End time must be after start time
     */
    public boolean validateDateLogic() {
        if (startTime == null || endTime == null) {
            return false;
        }
        return endTime.isAfter(startTime);
    }

    /**
     * Check if activity is expired (start time has passed)
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(startTime);
    }

    /**
     * Check if activity has ended
     */
    public boolean hasEnded() {
        return LocalDateTime.now().isAfter(endTime);
    }

    /**
     * Check if activity can be edited
     */
    public boolean canBeEdited() {
        return status.isEditable() && !isExpired();
    }

    /**
     * Check if activity can be approved
     */
    public boolean canBeApproved() {
        return status.canBeApproved() && !isExpired();
    }

    /**
     * Submit for review - transition from DRAFTING to PENDING_REVIEW
     */
    public void submitForReview() {
        if (status != ActivityStatus.DRAFTING && status != ActivityStatus.NEEDS_REVISION) {
            throw new IllegalStateException("只有草稿或需修正狀態的活動可以提交審核");
        }
        if (!validateDateLogic()) {
            throw new IllegalArgumentException("結束時間必須晚於開始時間");
        }
        this.status = ActivityStatus.PENDING_REVIEW;
        this.rejectionReason = null; // Clear previous rejection reason
    }

    /**
     * Approve activity - transition from PENDING_REVIEW to PUBLISHED
     */
    public void approve() {
        if (!canBeApproved()) {
            throw new IllegalStateException("此活動無法被核准");
        }
        this.status = ActivityStatus.PUBLISHED;
        this.rejectionReason = null;
    }

    /**
     * Reject activity - transition from PENDING_REVIEW to NEEDS_REVISION
     */
    public void reject(String reason) {
        if (status != ActivityStatus.PENDING_REVIEW) {
            throw new IllegalStateException("只有待審核狀態的活動可以被退回");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("退回原因不能為空");
        }
        this.status = ActivityStatus.NEEDS_REVISION;
        this.rejectionReason = reason;
    }

    /**
     * Mark activity as ended
     */
    public void markAsEnded() {
        if (status == ActivityStatus.PUBLISHED && hasEnded()) {
            this.status = ActivityStatus.ENDED;
        }
    }

    /**
     * Cancel activity
     */
    public void cancel() {
        if (status == ActivityStatus.ENDED) {
            throw new IllegalStateException("已結束的活動無法取消");
        }
        this.status = ActivityStatus.CANCELLED;
    }
}
