package com.dive.club.enums;

/**
 * User Role Enum for Spring Security
 */
public enum UserRole {
    /**
     * 訪客 - Can only view published activities
     */
    ROLE_GUEST,

    /**
     * 會員 - Can view details and register for activities
     */
    ROLE_MEMBER,

    /**
     * 社團幹部 - Can create, edit, and delete activities
     */
    ROLE_MANAGER,

    /**
     * 超級管理員 - Can audit activities and manage everything
     */
    ROLE_ADMIN
}
