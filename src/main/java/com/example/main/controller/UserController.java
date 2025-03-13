package com.example.main.controller;

import com.example.main.dto.UserDto;
import com.example.main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String email = payload.get("email");
        String password = payload.get("password");
        String phoneNumber = payload.get("phoneNumber");
        
        if (username == null || email == null || password == null) {
            return ResponseEntity.badRequest().build();
        }
        
        UserDto createdUser = userService.createUser(username, email, password, phoneNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String email = payload.get("email");
        String phoneNumber = payload.get("phoneNumber");
        
        return userService.updateUser(id, username, email, phoneNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/ban")
    public ResponseEntity<UserDto> banUser(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> payload,
            @RequestHeader("Moderator-Id") Long moderatorId) {
        
        Boolean banned = (Boolean) payload.get("banned");
        String reason = (String) payload.get("reason");
        
        if (banned == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return userService.banUser(id, banned, reason, moderatorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
    
    @PutMapping("/{id}/moderator")
    public ResponseEntity<UserDto> setModerator(
            @PathVariable Long id, 
            @RequestBody Map<String, Boolean> payload,
            @RequestHeader("Moderator-Id") Long adminId) {
        
        Boolean isModerator = payload.get("isModerator");
        
        if (isModerator == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return userService.setModerator(id, isModerator, adminId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }
    
    @PutMapping("/{id}/score")
    public ResponseEntity<UserDto> updateScore(@PathVariable Long id, @RequestBody Map<String, Float> payload) {
        Float scoreChange = payload.get("scoreChange");
        
        if (scoreChange == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return userService.updateScore(id, scoreChange)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            @RequestHeader("Moderator-Id") Long moderatorId) {
        
        if (userService.deleteUser(id, moderatorId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
    
    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");
        
        if (username == null || password == null) {
            return ResponseEntity.badRequest().build();
        }
        
        // Check if user is banned
        if (userService.isBanned(username)) {
            String reason = userService.getBanReason(username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "message", "Your account has been banned",
                            "reason", reason != null ? reason : "No reason provided"
                    ));
        }
        
        boolean authenticated = userService.authenticate(username, password);
        return ResponseEntity.ok(authenticated);
    }
    
    @GetMapping("/{id}/is-moderator")
    public ResponseEntity<Boolean> isModerator(@PathVariable Long id) {
        return ResponseEntity.ok(userService.isModerator(id));
    }
} 