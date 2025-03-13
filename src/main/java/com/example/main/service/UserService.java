package com.example.main.service;

import com.example.main.dto.UserDto;
import com.example.main.model.User;
import com.example.main.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<UserDto> getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public Optional<UserDto> getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDto);
    }
    
    public UserDto createUser(String username, String email, String password, String phoneNumber) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(password); // Store password as plain text for now
        user.setPhoneNumber(phoneNumber);
        user.setScore(0);
        user.setBanned(false);
        user.setModerator(false);
        
        User savedUser = userRepository.save(user);
        return convertToDto(savedUser);
    }
    
    public Optional<UserDto> updateUser(Long id, String username, String email, String phoneNumber) {
        return userRepository.findById(id)
                .map(user -> {
                    if (username != null) {
                        user.setUsername(username);
                    }
                    if (email != null) {
                        user.setEmail(email);
                    }
                    if (phoneNumber != null) {
                        user.setPhoneNumber(phoneNumber);
                    }
                    return convertToDto(userRepository.save(user));
                });
    }
    
    public Optional<UserDto> banUser(Long id, boolean banned, String reason, Long moderatorId) {
        Optional<User> moderatorOpt = userRepository.findById(moderatorId);
        if (moderatorOpt.isEmpty() || !moderatorOpt.get().isModerator()) {
            return Optional.empty(); // Only moderators can ban users
        }
        
        return userRepository.findById(id)
                .map(user -> {
                    // Don't allow banning moderators
                    if (user.isModerator()) {
                        return convertToDto(user);
                    }
                    
                    user.setBanned(banned);
                    if (banned) {
                        user.setBanReason(reason);
                        
                        // Send notifications
                        notificationService.sendBanNotificationEmail(user, reason);
                        notificationService.sendBanNotificationSMS(user, reason);
                    } else {
                        user.setBanReason(null);
                    }
                    
                    return convertToDto(userRepository.save(user));
                });
    }
    
    public Optional<UserDto> setModerator(Long id, boolean isModerator, Long adminId) {
        Optional<User> adminOpt = userRepository.findById(adminId);
        if (adminOpt.isEmpty() || !adminOpt.get().isModerator()) {
            return Optional.empty(); // Only existing moderators can promote others
        }
        
        return userRepository.findById(id)
                .map(user -> {
                    user.setModerator(isModerator);
                    return convertToDto(userRepository.save(user));
                });
    }
    
    public Optional<UserDto> updateScore(Long id, float scoreChange) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setScore(user.getScore() + scoreChange);
                    return convertToDto(userRepository.save(user));
                });
    }
    
    public boolean deleteUser(Long id, Long moderatorId) {
        Optional<User> moderatorOpt = userRepository.findById(moderatorId);
        if (moderatorOpt.isEmpty() || !moderatorOpt.get().isModerator()) {
            return false; // Only moderators can delete users
        }
        
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public boolean authenticate(String username, String password) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    if (user.isBanned()) {
                        // Authentication fails for banned users
                        return false;
                    }
                    return password.equals(user.getPasswordHash()); // Simple password comparison
                })
                .orElse(false);
    }
    
    public boolean isBanned(String username) {
        return userRepository.findByUsername(username)
                .map(User::isBanned)
                .orElse(false);
    }
    
    public String getBanReason(String username) {
        return userRepository.findByUsername(username)
                .map(User::getBanReason)
                .orElse(null);
    }
    
    public boolean isModerator(Long userId) {
        return userRepository.findById(userId)
                .map(User::isModerator)
                .orElse(false);
    }
    
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setScore(user.getScore());
        dto.setBanned(user.isBanned());
        dto.setModerator(user.isModerator());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setBanReason(user.getBanReason());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
    
    public User findUserEntityById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
} 