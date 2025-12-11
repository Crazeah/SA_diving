package com.dive.club.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for audit decision (approve/reject)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditDecisionDTO {

    @NotNull(message = "審核決策不能為空")
    private AuditAction action; // APPROVE or REJECT

    private String reason; // Required when action is REJECT

    public enum AuditAction {
        APPROVE,
        REJECT
    }

    /**
     * Validate that reason is provided when rejecting
     */
    public boolean isValid() {
        if (action == AuditAction.REJECT) {
            return reason != null && !reason.trim().isEmpty();
        }
        return true;
    }
}
