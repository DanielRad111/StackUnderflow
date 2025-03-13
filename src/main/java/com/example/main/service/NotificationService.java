package com.example.main.service;

import com.example.main.model.User;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    
    public void sendBanNotificationEmail(User user, String reason) {
        // In a real application, this would use JavaMailSender or a third-party email service
        System.out.println("Sending ban notification email to: " + user.getEmail());
        System.out.println("Subject: Your StackUnderflow account has been banned");
        System.out.println("Body: Dear " + user.getUsername() + ", your account has been banned for the following reason: " + reason);
    }
    
    public void sendBanNotificationSMS(User user, String reason) {
        // In a real application, this would use a third-party SMS service like Twilio
        if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
            System.out.println("Sending ban notification SMS to: " + user.getPhoneNumber());
            System.out.println("Message: Your StackUnderflow account has been banned for: " + reason);
        }
    }
} 