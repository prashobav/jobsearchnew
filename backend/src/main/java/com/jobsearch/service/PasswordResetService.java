package com.jobsearch.service;

import com.jobsearch.entity.PasswordResetToken;
import com.jobsearch.entity.User;
import com.jobsearch.repository.PasswordResetTokenRepository;
import com.jobsearch.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int MAX_RESET_ATTEMPTS_PER_HOUR = 3;
    
    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public boolean createPasswordResetToken(String email) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                // Don't reveal if email exists or not for security
                logger.warn("Password reset requested for non-existent email: {}", email);
                return true; // Return true to not reveal if email exists
            }
            
            User user = userOptional.get();
            
            // Check rate limiting
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            long recentAttempts = passwordResetTokenRepository.countByUserAndCreatedAtAfter(user, oneHourAgo);
            
            if (recentAttempts >= MAX_RESET_ATTEMPTS_PER_HOUR) {
                logger.warn("Too many password reset attempts for user: {}", user.getUsername());
                return false;
            }
            
            // Delete any existing tokens for this user
            passwordResetTokenRepository.deleteByUser(user);
            
            // Generate new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken(token, user);
            passwordResetTokenRepository.save(resetToken);
            
            // Send email (for now, just log the token - in production, send real email)
            logger.info("Password reset token for user {}: {}", user.getUsername(), token);
            logger.info("Reset link: http://localhost:3000/reset-password?token={}", token);
            
            // In production, uncomment this:
            // emailService.sendPasswordResetEmail(user.getEmail(), token);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error creating password reset token for email: {}", email, e);
            return false;
        }
    }
    
    public boolean validatePasswordResetToken(String token) {
        try {
            Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
            return tokenOptional.isPresent() && tokenOptional.get().isValid();
        } catch (Exception e) {
            logger.error("Error validating password reset token: {}", token, e);
            return false;
        }
    }
    
    public boolean resetPassword(String token, String newPassword) {
        try {
            Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByToken(token);
            
            if (tokenOptional.isEmpty()) {
                logger.warn("Invalid password reset token: {}", token);
                return false;
            }
            
            PasswordResetToken resetToken = tokenOptional.get();
            
            if (!resetToken.isValid()) {
                logger.warn("Expired or used password reset token: {}", token);
                return false;
            }
            
            // Update user password
            User user = resetToken.getUser();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            
            // Mark token as used
            resetToken.setUsed(true);
            passwordResetTokenRepository.save(resetToken);
            
            logger.info("Password reset successful for user: {}", user.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("Error resetting password with token: {}", token, e);
            return false;
        }
    }
    
    public void cleanupExpiredTokens() {
        try {
            passwordResetTokenRepository.deleteExpiredAndUsedTokens(LocalDateTime.now());
            logger.debug("Cleaned up expired and used password reset tokens");
        } catch (Exception e) {
            logger.error("Error cleaning up expired tokens", e);
        }
    }
}