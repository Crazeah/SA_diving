package com.dive.club.entity;

import com.dive.club.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Manager Entity - 社團幹部
 * Can create, edit, and delete activities
 */
@Entity
@Table(name = "managers")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "createdActivities")
public class Manager extends User {

    @Column(nullable = false)
    private String positionTitle; // 職位名稱 (e.g., "活動組長", "財務", etc.)

    @Column(nullable = false)
    private LocalDate joinDate; // 加入日期

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Activity> createdActivities = new ArrayList<>();

    public Manager(String name, String email, String password, String phoneNumber,
            String positionTitle, LocalDate joinDate) {
        this.setName(name);
        this.setEmail(email);
        this.setPassword(password);
        this.setPhoneNumber(phoneNumber);
        this.setRole(UserRole.ROLE_MANAGER);
        this.positionTitle = positionTitle;
        this.joinDate = joinDate;
    }
}
