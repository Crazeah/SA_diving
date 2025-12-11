package com.dive.club.enums;

/**
 * Activity Status Enum - State Machine Definition
 * 
 * State Flow:
 * DRAFTING -> PENDING_REVIEW -> PUBLISHED -> ENDED
 * | |
 * v v
 * NEEDS_REVISION CANCELLED
 */
public enum ActivityStatus {
    /**
     * 編輯中 - Initial state when creating activity
     */
    DRAFTING("編輯中"),

    /**
     * 待審核 - Submitted for approval by Manager
     */
    PENDING_REVIEW("待審核"),

    /**
     * 已發布 - Approved by SuperManager and visible to all users
     */
    PUBLISHED("已發布"),

    /**
     * 需修正 - Rejected by SuperManager, needs revision
     */
    NEEDS_REVISION("需修正"),

    /**
     * 已結束 - Activity end time has passed
     */
    ENDED("已結束"),

    /**
     * 已取消 - Cancelled by Manager or SuperManager
     */
    CANCELLED("已取消");

    private final String displayName;

    ActivityStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Check if activity can be edited
     */
    public boolean isEditable() {
        return this == DRAFTING || this == NEEDS_REVISION;
    }

    /**
     * Check if activity is visible to public
     */
    public boolean isPublicVisible() {
        return this == PUBLISHED || this == ENDED;
    }

    /**
     * Check if activity can be approved
     */
    public boolean canBeApproved() {
        return this == PENDING_REVIEW;
    }
}
