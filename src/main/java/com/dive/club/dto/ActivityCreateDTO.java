package com.dive.club.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating a new activity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityCreateDTO {

    @NotBlank(message = "活動標題不能為空")
    @Size(min = 3, max = 200, message = "活動標題長度必須在3-200字元之間")
    private String title;

    @NotBlank(message = "活動描述不能為空")
    private String description;

    @NotBlank(message = "活動類別不能為空")
    private String category;

    @NotNull(message = "開始時間不能為空")
    @Future(message = "開始時間必須是未來時間")
    private LocalDateTime startTime;

    @NotNull(message = "結束時間不能為空")
    private LocalDateTime endTime;

    @NotBlank(message = "活動地點不能為空")
    private String location;

    @NotNull(message = "人數上限不能為空")
    @Min(value = 1, message = "人數上限至少為1人")
    @Max(value = 1000, message = "人數上限不能超過1000人")
    private Integer maxParticipants;

    @NotNull(message = "費用不能為空")
    @DecimalMin(value = "0.0", message = "費用不能為負數")
    private BigDecimal cost;

    private String qualifications;

    private String imageUrl;

    /**
     * Custom validation for date logic
     */
    @AssertTrue(message = "結束時間必須晚於開始時間")
    public boolean isEndTimeAfterStartTime() {
        if (startTime == null || endTime == null) {
            return true; // Let @NotNull handle null checks
        }
        return endTime.isAfter(startTime);
    }
}
