package com.example.main.controller;

import com.example.main.dto.AnswerDto;
import com.example.main.service.AnswerService;
import com.example.main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/answers")
public class AnswerController {
    @Autowired
    private AnswerService answerService;
    @Autowired
    private UserService userService;

    @GetMapping("/all")
    public ResponseEntity<List<AnswerDto>> getAllAnswers(){
        return ResponseEntity.ok(answerService.getAllAnswers());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<AnswerDto> getAnswerById(@PathVariable Long id){
        return answerService.getAnswerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerDto>> getAnswersByQuestion(@PathVariable Long questionId){
        return ResponseEntity.ok(answerService.getAnswersByQuestion(questionId));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<AnswerDto>> getAnswersByAuthor(@PathVariable Long authorId){
        return ResponseEntity.ok(answerService.getAnswersByAuthor(authorId));
    }

    @PostMapping("/create")
    public ResponseEntity<AnswerDto> createAnswer(@RequestBody Map<String, Object> body){
        Long questionId = Long.valueOf(body.get("id").toString());
        Long authorId = Long.valueOf(body.get("authorId").toString());
        String text = (String) body.get("text");
        String image = (String) body.get("image");

        if(questionId == null || authorId == null || text == null){
            return  ResponseEntity.badRequest().build();
        }

        AnswerDto createdAnswer = answerService.createAnswer(questionId, authorId, text, image);
        if(createdAnswer == null){
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(createdAnswer);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<AnswerDto> updateAnswer(@PathVariable Long id, @RequestBody Map<String, String> body, @RequestParam Long userId){
        if(!isAuthorOrModerator(id, userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String text = body.get("text");
        String image = body.get("image");

        return answerService.updateAnswer(id,text,image)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long id, @RequestParam Long userId){
        if(!isAuthorOrModerator(id,userId)){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if(answerService.deleteAnswer(id)){
            return ResponseEntity.noContent().build();
        }else{
            return ResponseEntity.notFound().build();
        }
    }

    private boolean isAuthorOrModerator(Long answerId, Long userId) {
        return answerService.getAnswerById(answerId)
                .map(a -> a.getAuthorId().equals(userId) || userService.isModerator(userService.getUserById(userId).get().getUsername()))
                .orElse(false);
    }
}
