package com.example.main.controller;

import com.example.main.dto.UserDto;
import com.example.main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id){
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username){
        return userService.getUserByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(@RequestBody Map<String,String> body){
        String username = body.get("username");
        String email = body.get("email");
        String password = body.get("password");
        String phoneNumber = body.get("phoneNumber");

        if(username == null || email == null || password == null){
            return ResponseEntity.badRequest().build();
        }

        UserDto userDto = userService.createUser(username,email,password,phoneNumber);
        return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @RequestBody Map<String,String> body){
        String username = body.get("username");
        String email = body.get("email");
        String phoneNumber = body.get("phoneNumber");

        return userService.updateUser(id,username,email,phoneNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/ban/{id}")
    public ResponseEntity<UserDto> banUser(@PathVariable Long id, @RequestBody Map<String, Object> body){
        Boolean banned = (Boolean) body.get("banned");
        String reason = (String) body.get("reason");
        Number moderatorIdNumber = (Number) body.get("moderatorId");
        Long moderatorId = moderatorIdNumber != null ? moderatorIdNumber.longValue() : null;

        if(banned == null){
            return ResponseEntity.badRequest().build();
        }

        return userService.banUser(id, banned, reason, moderatorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @PutMapping("/{id}/moderator")
    public ResponseEntity<UserDto> setModerator(@PathVariable Long id, @RequestBody Map<String, Object> body){
        Boolean isModerator = (Boolean) body.get("isModerator");
        Number moderatorIdNumber = (Number) body.get("moderatorId");
        Long moderatorId = moderatorIdNumber != null ? moderatorIdNumber.longValue() : null;

        if(isModerator == null){
            return ResponseEntity.badRequest().build();
        }

        return userService.setModerator(id, isModerator, moderatorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, @RequestParam Long moderatorId){
        if(userService.deleteUser(id,moderatorId)){
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody Map<String,String> body){
        String username = body.get("username");
        String password = body.get("password");

        if(username == null || password == null){
            return ResponseEntity.badRequest().build();
        }

        if(userService.isBanned(username)){
            String reason = userService.getBanReason(username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Your account has been banned",
                                 "reason", reason != null ? reason : "No reason provided"
                    ));
        }

        boolean authenticated = userService.authenticate(username, password);
        return ResponseEntity.ok(authenticated);
    }
}
