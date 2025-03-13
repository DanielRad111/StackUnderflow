package com.example.main.controller;

import com.example.main.dto.AnswerDto;
import com.example.main.service.AnswerService;
import com.example.main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {
    
    @Autowired
    private AnswerService answerService;
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<AnswerDto>> getAllAnswers() {
        return ResponseEntity.ok(answerService.getAllAnswers());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AnswerDto> getAnswerById(@PathVariable Long id) {
        return answerService.getAnswerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerDto>> getAnswersByQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(answerService.getAnswersByQuestion(questionId));
    }
    
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<AnswerDto>> getAnswersByAuthor(@PathVariable Long authorId) {
        return ResponseEntity.ok(answerService.getAnswersByAuthor(authorId));
    }
    
    @PostMapping
    public ResponseEntity<AnswerDto> createAnswer(@RequestBody Map<String, Object> payload) {
        Long questionId = Long.valueOf(payload.get("questionId").toString());
        Long authorId = Long.valueOf(payload.get("authorId").toString());
        String text = (String) payload.get("text");
        String image = (String) payload.get("image");
        
        if (questionId == null || authorId == null || text == null) {
            return ResponseEntity.badRequest().build();
        }
        
        AnswerDto createdAnswer = answerService.createAnswer(questionId, authorId, text, image);
        if (createdAnswer == null) {
            return ResponseEntity.badRequest().build();
        }
        
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAnswer);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AnswerDto> updateAnswer(
            @PathVariable Long id, 
            @RequestBody Map<String, String> payload,
            @RequestHeader("User-Id") Long userId) {
        
        // Check if user is the author or a moderator
        if (!isAuthorOrModerator(id, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String text = payload.get("text");
        String image = payload.get("image");
        
        return answerService.updateAnswer(id, text, image)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable Long id,
            @RequestHeader("User-Id") Long userId) {
        
        // Check if user is the author or a moderator
        if (!isAuthorOrModerator(id, userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (answerService.deleteAnswer(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    private boolean isAuthorOrModerator(Long answerId, Long userId) {
        return answerService.getAnswerById(answerId)
                .map(a -> a.getAuthorId().equals(userId) || userService.isModerator(userId))
                .orElse(false);
    }
} 