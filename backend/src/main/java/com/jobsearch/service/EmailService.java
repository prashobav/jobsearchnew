package com.jobsearch.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    public void sendPasswordResetEmail(String email, String token) {
        // For now, just log the email content
        // In production, integrate with email service like SendGrid, AWS SES, etc.
        
        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        
        logger.info("=== PASSWORD RESET EMAIL ===");
        logger.info("To: {}", email);
        logger.info("Subject: Password Reset Request");
        logger.info("Reset Link: {}", resetLink);
        logger.info("Token: {}", token);
        logger.info("This link will expire in 24 hours.");
        logger.info("=== END EMAIL ===");
        
        // TODO: Implement actual email sending
        // Example with Spring Mail:
        /*
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        
        helper.setFrom("noreply@jobsearch.com");
        helper.setTo(email);
        helper.setSubject("Password Reset Request");
        
        String htmlContent = buildPasswordResetEmailTemplate(resetLink);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
        */
    }
    
    private String buildPasswordResetEmailTemplate(String resetLink) {
        return String.format("""
            <html>
            <body>
                <h2>Password Reset Request</h2>
                <p>You have requested to reset your password. Click the link below to reset it:</p>
                <p><a href="%s">Reset Password</a></p>
                <p>This link will expire in 24 hours.</p>
                <p>If you did not request this reset, please ignore this email.</p>
            </body>
            </html>
            """, resetLink);
    }
}