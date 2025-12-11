package com.dive.club.service;

import com.dive.club.entity.Activity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Email Service for sending notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.name:Diving Club Management System}")
    private String appName;

    @Value("${app.admin.email:admin@diveclub.com}")
    private String adminEmail;

    /**
     * Send email notification when activity is approved
     */
    public void sendApprovalNotification(Activity activity) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(adminEmail);
            message.setTo(activity.getCreator().getEmail());
            message.setSubject(String.format("[%s] 活動審核通過 - %s", appName, activity.getTitle()));

            String content = String.format(
                    "親愛的 %s，\n\n" +
                            "您的活動已通過審核！\n\n" +
                            "活動名稱：%s\n" +
                            "活動時間：%s 至 %s\n" +
                            "活動地點：%s\n\n" +
                            "活動已發布，會員現在可以瀏覽並報名參加。\n\n" +
                            "祝活動順利！\n\n" +
                            "%s 管理團隊",
                    activity.getCreator().getName(),
                    activity.getTitle(),
                    activity.getStartTime(),
                    activity.getEndTime(),
                    activity.getLocation(),
                    appName);

            message.setText(content);
            mailSender.send(message);

            log.info("Approval notification sent to {} for activity {}",
                    activity.getCreator().getEmail(), activity.getActivityId());
        } catch (Exception e) {
            log.error("Failed to send approval notification", e);
            // Don't throw exception - email failure shouldn't break the approval process
        }
    }

    /**
     * Send email notification when activity is rejected
     */
    public void sendRejectionNotification(Activity activity, String reason) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(adminEmail);
            message.setTo(activity.getCreator().getEmail());
            message.setSubject(String.format("[%s] 活動需要修正 - %s", appName, activity.getTitle()));

            String content = String.format(
                    "親愛的 %s，\n\n" +
                            "您的活動需要修正後重新提交。\n\n" +
                            "活動名稱：%s\n" +
                            "退回原因：%s\n\n" +
                            "請根據上述原因修正活動內容後，重新提交審核。\n\n" +
                            "如有任何問題，請聯繫管理員。\n\n" +
                            "%s 管理團隊",
                    activity.getCreator().getName(),
                    activity.getTitle(),
                    reason,
                    appName);

            message.setText(content);
            mailSender.send(message);

            log.info("Rejection notification sent to {} for activity {}",
                    activity.getCreator().getEmail(), activity.getActivityId());
        } catch (Exception e) {
            log.error("Failed to send rejection notification", e);
        }
    }

    /**
     * Send email notification when activity is submitted for review
     */
    public void sendSubmissionNotification(Activity activity) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(adminEmail);
            message.setTo(activity.getCreator().getEmail());
            message.setSubject(String.format("[%s] 活動已提交審核 - %s", appName, activity.getTitle()));

            String content = String.format(
                    "親愛的 %s，\n\n" +
                            "您的活動已成功提交審核！\n\n" +
                            "活動名稱：%s\n" +
                            "提交時間：%s\n\n" +
                            "管理員將盡快審核您的活動，審核結果將透過郵件通知您。\n\n" +
                            "%s 管理團隊",
                    activity.getCreator().getName(),
                    activity.getTitle(),
                    activity.getCreatedAt(),
                    appName);

            message.setText(content);
            mailSender.send(message);

            log.info("Submission notification sent to {} for activity {}",
                    activity.getCreator().getEmail(), activity.getActivityId());
        } catch (Exception e) {
            log.error("Failed to send submission notification", e);
        }
    }

    /**
     * Notify admin about new activity pending review
     */
    public void notifyAdminNewSubmission(Activity activity) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(adminEmail);
            message.setTo(adminEmail);
            message.setSubject(String.format("[%s] 新活動待審核 - %s", appName, activity.getTitle()));

            String content = String.format(
                    "管理員您好，\n\n" +
                            "有新的活動待審核：\n\n" +
                            "活動名稱：%s\n" +
                            "建立者：%s (%s)\n" +
                            "活動時間：%s 至 %s\n" +
                            "活動地點：%s\n\n" +
                            "請登入系統進行審核。\n\n" +
                            "%s",
                    activity.getTitle(),
                    activity.getCreator().getName(),
                    activity.getCreator().getEmail(),
                    activity.getStartTime(),
                    activity.getEndTime(),
                    activity.getLocation(),
                    appName);

            message.setText(content);
            mailSender.send(message);

            log.info("Admin notification sent for new activity {}", activity.getActivityId());
        } catch (Exception e) {
            log.error("Failed to send admin notification", e);
        }
    }
}
