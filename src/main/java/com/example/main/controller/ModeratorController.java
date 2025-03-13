package com.example.main.controller;

import com.example.main.dto.AnswerDto;
import com.example.main.dto.QuestionDto;
import com.example.main.service.AnswerService;
import com.example.main.service.QuestionService;
import com.example.main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/moderator")
public class ModeratorController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private AnswerService answerService;
    
    // Moderator can edit any question
    @PutMapping("/questions/{id}")
    public ResponseEntity<QuestionDto> editQuestion(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload,
            @RequestHeader("Moderator-Id") Long moderatorId) {
        
        if (!userService.isModerator(moderatorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String title = payload.get("title");
        String text = payload.get("text");
        String image = payload.get("image");
        String tags = payload.get("tags");
        String status = payload.get("status");
        
        return questionService.updateQuestion(id, title, text, image, tags, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Moderator can delete any question
    @DeleteMapping("/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @RequestHeader("Moderator-Id") Long moderatorId) {
        
        if (!userService.isModerator(moderatorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (questionService.deleteQuestion(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    // Moderator can edit any answer
    @PutMapping("/answers/{id}")
    public ResponseEntity<AnswerDto> editAnswer(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload,
            @RequestHeader("Moderator-Id") Long moderatorId) {
        
        if (!userService.isModerator(moderatorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String text = payload.get("text");
        String image = payload.get("image");
        
        return answerService.updateAnswer(id, text, image)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    // Moderator can delete any answer
    @DeleteMapping("/answers/{id}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable Long id,
            @RequestHeader("Moderator-Id") Long moderatorId) {
        
        if (!userService.isModerator(moderatorId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (answerService.deleteAnswer(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
} 