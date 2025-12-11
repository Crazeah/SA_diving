package com.dive.club.config;

import com.dive.club.entity.Activity;
import com.dive.club.entity.Manager;
import com.dive.club.entity.SuperManager;
import com.dive.club.entity.User;
import com.dive.club.enums.ActivityStatus;
import com.dive.club.enums.UserRole;
import com.dive.club.repository.ActivityRepository;
import com.dive.club.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Initialization
 * Populates the database with sample data for testing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Initializing database with sample data...");
            initializeData();
            log.info("Database initialization completed!");
        } else {
            log.info("Database already contains data, skipping initialization.");
        }
    }

    private void initializeData() {
        // Create SuperManager (Admin)
        SuperManager admin = new SuperManager(
                "管理員",
                "admin@diveclub.com",
                passwordEncoder.encode("admin123"),
                "0912345678",
                "社長",
                LocalDate.of(2023, 1, 1),
                "社長");
        userRepository.save(admin);
        log.info("Created SuperManager: {}", admin.getEmail());

        // Create Managers
        Manager manager1 = new Manager(
                "王小明",
                "manager1@diveclub.com",
                passwordEncoder.encode("manager123"),
                "0923456789",
                "活動組長",
                LocalDate.of(2023, 3, 1));
        userRepository.save(manager1);
        log.info("Created Manager: {}", manager1.getEmail());

        Manager manager2 = new Manager(
                "李美麗",
                "manager2@diveclub.com",
                passwordEncoder.encode("manager123"),
                "0934567890",
                "訓練組長",
                LocalDate.of(2023, 5, 1));
        userRepository.save(manager2);
        log.info("Created Manager: {}", manager2.getEmail());

        // Create regular member
        User member = new User();
        member.setName("張三");
        member.setEmail("member@diveclub.com");
        member.setPassword(passwordEncoder.encode("member123"));
        member.setPhoneNumber("0945678901");
        member.setRole(UserRole.ROLE_MEMBER);
        userRepository.save(member);
        log.info("Created Member: {}", member.getEmail());

        // Create sample activities
        createSampleActivities(manager1, manager2);
    }

    private void createSampleActivities(Manager manager1, Manager manager2) {
        // Published Activity 1
        Activity activity1 = new Activity();
        activity1.setTitle("墾丁潛水體驗活動");
        activity1.setDescription("歡迎所有對潛水有興趣的同學參加！我們將前往墾丁進行兩天一夜的潛水體驗，由專業教練指導，適合初學者。");
        activity1.setCategory("潛水訓練");
        activity1.setStartTime(LocalDateTime.now().plusDays(30));
        activity1.setEndTime(LocalDateTime.now().plusDays(31));
        activity1.setLocation("墾丁國家公園海域");
        activity1.setMaxParticipants(20);
        activity1.setCost(new BigDecimal("3500.00"));
        activity1.setQualifications("無需任何經驗，歡迎新手");
        activity1.setImageUrl("https://example.com/images/diving1.jpg");
        activity1.setStatus(ActivityStatus.PUBLISHED);
        activity1.setCreator(manager1);
        activityRepository.save(activity1);
        log.info("Created activity: {}", activity1.getTitle());

        // Published Activity 2
        Activity activity2 = new Activity();
        activity2.setTitle("綠島進階潛水訓練");
        activity2.setDescription("針對已有 OW 證照的會員，進行進階潛水技巧訓練，包括深潛、夜潛等項目。");
        activity2.setCategory("進階訓練");
        activity2.setStartTime(LocalDateTime.now().plusDays(45));
        activity2.setEndTime(LocalDateTime.now().plusDays(47));
        activity2.setLocation("綠島海域");
        activity2.setMaxParticipants(15);
        activity2.setCost(new BigDecimal("8500.00"));
        activity2.setQualifications("須持有 OW 證照");
        activity2.setImageUrl("https://example.com/images/diving2.jpg");
        activity2.setStatus(ActivityStatus.PUBLISHED);
        activity2.setCreator(manager1);
        activityRepository.save(activity2);
        log.info("Created activity: {}", activity2.getTitle());

        // Pending Review Activity
        Activity activity3 = new Activity();
        activity3.setTitle("小琉球海洋保育活動");
        activity3.setDescription("參與海洋保育，協助清理海底垃圾，並了解海洋生態保護的重要性。");
        activity3.setCategory("海洋保育");
        activity3.setStartTime(LocalDateTime.now().plusDays(20));
        activity3.setEndTime(LocalDateTime.now().plusDays(21));
        activity3.setLocation("小琉球海域");
        activity3.setMaxParticipants(30);
        activity3.setCost(new BigDecimal("2000.00"));
        activity3.setQualifications("無限制");
        activity3.setImageUrl("https://example.com/images/conservation.jpg");
        activity3.setStatus(ActivityStatus.PENDING_REVIEW);
        activity3.setCreator(manager2);
        activityRepository.save(activity3);
        log.info("Created activity: {}", activity3.getTitle());

        // Drafting Activity
        Activity activity4 = new Activity();
        activity4.setTitle("東北角浮潛活動");
        activity4.setDescription("輕鬆的浮潛活動，適合全體會員參加。");
        activity4.setCategory("休閒活動");
        activity4.setStartTime(LocalDateTime.now().plusDays(15));
        activity4.setEndTime(LocalDateTime.now().plusDays(15).plusHours(6));
        activity4.setLocation("東北角海域");
        activity4.setMaxParticipants(25);
        activity4.setCost(new BigDecimal("1500.00"));
        activity4.setQualifications("無限制");
        activity4.setStatus(ActivityStatus.DRAFTING);
        activity4.setCreator(manager2);
        activityRepository.save(activity4);
        log.info("Created activity: {}", activity4.getTitle());

        // Needs Revision Activity
        Activity activity5 = new Activity();
        activity5.setTitle("蘭嶼深潛探險");
        activity5.setDescription("前往蘭嶼進行深潛探險活動。");
        activity5.setCategory("進階訓練");
        activity5.setStartTime(LocalDateTime.now().plusDays(60));
        activity5.setEndTime(LocalDateTime.now().plusDays(63));
        activity5.setLocation("蘭嶼海域");
        activity5.setMaxParticipants(12);
        activity5.setCost(new BigDecimal("12000.00"));
        activity5.setQualifications("須持有 AOW 證照");
        activity5.setStatus(ActivityStatus.NEEDS_REVISION);
        activity5.setRejectionReason("活動描述不夠詳細，請補充更多資訊，包括住宿安排、行程細節等。");
        activity5.setCreator(manager1);
        activityRepository.save(activity5);
        log.info("Created activity: {}", activity5.getTitle());
    }
}
