package com.dive.club.dto;

import com.dive.club.entity.Activity;
import com.dive.club.enums.ActivityStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for activity response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityResponseDTO {

    private Long activityId;
    private String title;
    private String description;
    private String category;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String location;
    private Integer maxParticipants;
    private BigDecimal cost;
    private String qualifications;
    private String imageUrl;
    private ActivityStatus status;
    private String rejectionReason;
    private String creatorName;
    private String creatorEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Convert Activity entity to DTO
     */
    public static ActivityResponseDTO fromEntity(Activity activity) {
        ActivityResponseDTO dto = new ActivityResponseDTO();
        dto.setActivityId(activity.getActivityId());
        dto.setTitle(activity.getTitle());
        dto.setDescription(activity.getDescription());
        dto.setCategory(activity.getCategory());
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        dto.setLocation(activity.getLocation());
        dto.setMaxParticipants(activity.getMaxParticipants());
        dto.setCost(activity.getCost());
        dto.setQualifications(activity.getQualifications());
        dto.setImageUrl(activity.getImageUrl());
        dto.setStatus(activity.getStatus());
        dto.setRejectionReason(activity.getRejectionReason());
        dto.setCreatorName(activity.getCreator().getName());
        dto.setCreatorEmail(activity.getCreator().getEmail());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUpdatedAt(activity.getUpdatedAt());
        return dto;
    }
}
