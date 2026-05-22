package com.placementpro.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Sends a status update email to the student.
     * Fails gracefully if email is not configured.
     */
    public void sendStatusEmail(String toEmail, String studentName, String roleName,
                                String companyName, String status) {
        if (!isMailConfigured()) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PlacementPro - Application Status Update: " + status);

            String emoji = getStatusEmoji(status);
            String htmlBody = buildStatusEmailHtml(studentName, roleName, companyName, status, emoji);

            helper.setText(htmlBody, true);
            mailSender.send(message);
            System.out.println("[EMAIL] Sent status update to " + toEmail);
        } catch (Exception e) {
            System.err.println("[EMAIL] Failed to send to " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * Sends a generic notification email.
     */
    public void sendNotificationEmail(String toEmail, String recipientName,
                                      String subject, String messageText) {
        if (!isMailConfigured()) return;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("PlacementPro - " + subject);

            String htmlBody = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                + "<div style='background:linear-gradient(135deg,#4F46E5,#2563EB);padding:24px;border-radius:16px 16px 0 0;'>"
                + "<h1 style='color:white;margin:0;font-size:20px;'>PlacementPro Notification</h1></div>"
                + "<div style='background:#fff;padding:24px;border:1px solid #E2E8F0;border-top:none;border-radius:0 0 16px 16px;'>"
                + "<p>Hi <strong>" + recipientName + "</strong>,</p>"
                + "<p>" + messageText + "</p>"
                + "<hr style='border:none;border-top:1px solid #E2E8F0;margin:20px 0;'>"
                + "<p style='color:#64748B;font-size:12px;'>This is an automated notification from PlacementPro.</p>"
                + "</div></div>";

            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EMAIL] Failed to send notification to " + toEmail + ": " + e.getMessage());
        }
    }

    private boolean isMailConfigured() {
        return mailSender != null && fromEmail != null
               && !fromEmail.isEmpty()
               && !fromEmail.contains("your-email")
               && !fromEmail.contains("placementpro.notify");
    }

    private String getStatusEmoji(String status) {
        return switch (status) {
            case "Shortlisted" -> "📋";
            case "Interview Scheduled" -> "📅";
            case "Selected" -> "🎉";
            case "Rejected" -> "📌";
            default -> "📢";
        };
    }

    private String buildStatusEmailHtml(String name, String role, String company,
                                        String status, String emoji) {
        String statusColor = switch (status) {
            case "Selected" -> "#059669";
            case "Shortlisted" -> "#D97706";
            case "Interview Scheduled" -> "#7C3AED";
            case "Rejected" -> "#DC2626";
            default -> "#4F46E5";
        };

        return "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
            + "<div style='background:linear-gradient(135deg,#4F46E5,#2563EB);padding:24px;border-radius:16px 16px 0 0;text-align:center;'>"
            + "<h1 style='color:white;margin:0;font-size:22px;'>" + emoji + " Application Update</h1></div>"
            + "<div style='background:#fff;padding:32px;border:1px solid #E2E8F0;border-top:none;border-radius:0 0 16px 16px;'>"
            + "<p style='font-size:16px;'>Hi <strong>" + name + "</strong>,</p>"
            + "<p>Your application status has been updated:</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0;'>"
            + "<tr><td style='padding:8px 12px;background:#F8FAFC;font-weight:600;border-radius:8px 0 0 0;'>Role</td>"
            + "<td style='padding:8px 12px;background:#F8FAFC;border-radius:0 8px 0 0;'>" + role + "</td></tr>"
            + "<tr><td style='padding:8px 12px;font-weight:600;'>Company</td>"
            + "<td style='padding:8px 12px;'>" + company + "</td></tr>"
            + "<tr><td style='padding:8px 12px;background:#F8FAFC;font-weight:600;border-radius:0 0 0 8px;'>Status</td>"
            + "<td style='padding:8px 12px;background:#F8FAFC;border-radius:0 0 8px 0;'>"
            + "<span style='background:" + statusColor + ";color:white;padding:4px 12px;border-radius:20px;font-weight:700;font-size:13px;'>"
            + status + "</span></td></tr></table>"
            + "<p style='color:#64748B;font-size:13px;margin-top:24px;'>Log in to PlacementPro for full details.</p>"
            + "</div></div>";
    }
}
