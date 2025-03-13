package com.example.main.controller;

import com.example.main.dto.QuestionDto;
import com.example.main.service.QuestionService;
import com.example.main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<QuestionDto>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<QuestionDto> getQuestionById(@PathVariable Long id) {
        return questionService.getQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<QuestionDto>> getQuestionsByAuthor(@PathVariable Long authorId) {
        return ResponseEntity.ok(questionService.getQuestionsByAuthor(authorId));
    }
    
    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<QuestionDto>> getQuestionsByTag(@PathVariable String tag) {
        return ResponseEntity.ok(questionService.getQuestionsByTag(tag));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<QuestionDto>> getQuestionsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(questionService.getQuestionsByStatus(status));
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<QuestionDto>> searchQuestions(@RequestParam String keyword) {
        return ResponseEntity.ok(questionService.searchQuestions(keyword));
    }
    
    @PostMapping
    public ResponseEntity<QuestionDto> createQuestion(@RequestBody Map<String, Object> payload) {
        Long authorId = Long.valueOf(payload.get("authorId").toString());
        String title = (String) payload.get("title");
        String text = (String) payload.get("text");
        String image = (String) payload.get("image");
        String tags = (String) payload.get("tags");
        
        if (authorId == null || title == null || text == null) {
            return ResponseEntity.badRequest().build();
        }
        
        QuestionDto createdQuestion = questionService.createQuestion(authorId, title, text, image, tags);
        if (createdQuestion == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<QuestionDto> updateQuestion(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload,
            @RequestHeader("User-Id") Long userId) {
        
        // Check if user is the author or a moderator
        if (!isAuthorOrModerator(id, userId)) {
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
    
    @PutMapping("/{questionId}/accept/{answerId}")
    public ResponseEntity<QuestionDto> acceptAnswer(@PathVariable Long questionId, @PathVariable Long answerId) {
        return questionService.acceptAnswer(questionId, answerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        
        // Check if user is the author or a moderator
        if (!isAuthorOrModerator(id, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (questionService.deleteQuestion(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    private boolean isAuthorOrModerator(Long questionId, Long userId) {
        return questionService.getQuestionById(questionId)
                .map(q -> q.getAuthorId().equals(userId) || userService.isModerator(userId))
                .orElse(false);
    }
} 