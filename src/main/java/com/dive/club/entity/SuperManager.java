package com.dive.club.entity;

import com.dive.club.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * SuperManager Entity - 超級管理員
 * Can audit activities and has highest privileges
 */
@Entity
@Table(name = "super_managers")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class SuperManager extends Manager {

    @Column(nullable = false)
    private String title; // 管理員頭銜 (e.g., "社長", "副社長", etc.)

    public SuperManager(String name, String email, String password, String phoneNumber,
            String positionTitle, LocalDate joinDate, String title) {
        super(name, email, password, phoneNumber, positionTitle, joinDate);
        this.setRole(UserRole.ROLE_ADMIN);
        this.title = title;
    }
}
