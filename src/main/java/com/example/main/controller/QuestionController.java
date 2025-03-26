package com.example.main.controller;

import com.example.main.dto.QuestionDto;
import com.example.main.model.User;
import com.example.main.service.QuestionService;
import com.example.main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questions")
public class QuestionController {
    @Autowired
    private QuestionService questionService;

    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<QuestionDto>> getAllQuestions(){
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @GetMapping("/find/{id}")
    public ResponseEntity<QuestionDto> getQuestionById(@PathVariable Long id){
        return questionService.getQuestionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<QuestionDto>> getQuestionsByAuthor(@PathVariable Long authorId){
        return ResponseEntity.ok(questionService.getQuestionsByAuthor(authorId));
    }

    @GetMapping("/tag/{tagName}")
    public ResponseEntity<List<QuestionDto>> getQuestionsByTag(@PathVariable String tagName){
        return ResponseEntity.ok(questionService.getQuestionsByTag(tagName));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<QuestionDto>> getQuestionsByStatus(@PathVariable String status){
        return ResponseEntity.ok(questionService.getQuestionsByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<List<QuestionDto>> searchQuestions(@RequestParam String keyword){
        return ResponseEntity.ok(questionService.searchQuestions(keyword));
    }

    @PostMapping("/create")
    public ResponseEntity<QuestionDto> createQuestion(@RequestBody Map<String,Object> body){
        Long authorId = Long.valueOf(body.get("authorId").toString());
        String title = body.get("title").toString();
        String text = body.get("text").toString();
        String image = body.get("image").toString();
        String tags = (String) body.get("tags");

        if(authorId == null || title == null || text == null){
            return ResponseEntity.badRequest().build();
        }

        QuestionDto createdQuestion = questionService.createQuestion(authorId,title,text,image,tags);
        if(createdQuestion == null){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<QuestionDto> updateQuestion(@PathVariable Long id, @RequestBody Map<String, String> body, @RequestParam Long userId){
        if(!isAuthorOrModerator(id, userId)){
            return ResponseEntity.badRequest().build();
        }
        String title = body.get("title");
        String text = body.get("text");
        String image = body.get("image");
        String tags = body.get("tags");
        String status = body.get("status");

        return questionService.updateQuestion(id,title,text,image,tags,status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @PutMapping("/{questionId}/accept/{answerId}")
    public ResponseEntity<QuestionDto> acceptAnswer(@PathVariable Long questionId, @PathVariable Long answerId){
        return questionService.acceptAnswer(questionId,answerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteQuestion(@PathVariable Long id, @RequestParam Long userId){
        if(!isAuthorOrModerator(id, userId)){
            return ResponseEntity.badRequest().build();
        }
        return questionService.deleteQuestion(id) ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

    private boolean isAuthorOrModerator(Long questionId, Long userId){
        User user = userService.findUserEntityById(userId);
        return questionService.getQuestionById(questionId)
                .map(question -> question.getAuthorId().equals(userId) || userService.isModerator(user.getUsername()))
                .orElse(false);
    }
}
