package com.example.main.service;

import com.example.main.dto.QuestionDto;
import com.example.main.dto.TagDto;
import com.example.main.model.Question;
import com.example.main.model.Tag;
import com.example.main.model.User;
import com.example.main.model.Vote;
import com.example.main.repository.QuestionRepository;
import com.example.main.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    
    @Autowired
    private QuestionRepository questionRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TagService tagService;
    
    @Autowired
    private VoteRepository voteRepository;
    
    public List<QuestionDto> getAllQuestions() {
        return questionRepository.findAll().stream()
                .sorted((q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<QuestionDto> getQuestionById(Long id) {
        return questionRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public List<QuestionDto> getQuestionsByAuthor(Long authorId) {
        User author = userService.findUserEntityById(authorId);
        if (author == null) {
            return List.of();
        }
        return questionRepository.findByAuthor(author).stream()
                .sorted((q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<QuestionDto> getQuestionsByTag(String tagName) {
        Optional<Tag> tag = tagService.findTagEntityByName(tagName);
        if (tag.isEmpty()) {
            return List.of();
        }
        return tag.get().getQuestions().stream()
                .sorted((q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<QuestionDto> getQuestionsByStatus(String status) {
        return questionRepository.findByStatus(status).stream()
                .sorted((q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public List<QuestionDto> searchQuestions(String keyword) {
        return questionRepository.findByTitleContainingOrTextContaining(keyword, keyword).stream()
                .sorted((q1, q2) -> q2.getCreatedAt().compareTo(q1.getCreatedAt()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public QuestionDto createQuestion(Long authorId, String title, String text, String image, String tagString) {
        User author = userService.findUserEntityById(authorId);
        if (author == null) {
            return null;
        }
        
        Question question = new Question();
        question.setAuthor(author);
        question.setTitle(title);
        question.setText(text);
        question.setImage(image);
        question.setStatus("received");
        
        // Process tags
        if (tagString != null && !tagString.trim().isEmpty()) {
            List<String> tagNames = Arrays.asList(tagString.split(","));
            List<Tag> tags = new ArrayList<>();
            
            for (String tagName : tagNames) {
                String trimmedTagName = tagName.trim();
                if (!trimmedTagName.isEmpty()) {
                    Tag tag = tagService.findOrCreateTag(trimmedTagName);
                    tags.add(tag);
                }
            }
            
            question.setTags(tags);
        }
        
        Question savedQuestion = questionRepository.save(question);
        return convertToDto(savedQuestion);
    }
    
    public Optional<QuestionDto> updateQuestion(Long id, String title, String text, String image, String tagString, String status) {
        return questionRepository.findById(id)
                .map(question -> {
                    if (title != null) {
                        question.setTitle(title);
                    }
                    if (text != null) {
                        question.setText(text);
                    }
                    if (image != null) {
                        question.setImage(image);
                    }
                    if (status != null) {
                        question.setStatus(status);
                    }
                    
                    // Process tags
                    if (tagString != null) {
                        List<String> tagNames = Arrays.asList(tagString.split(","));
                        List<Tag> tags = new ArrayList<>();
                        
                        for (String tagName : tagNames) {
                            String trimmedTagName = tagName.trim();
                            if (!trimmedTagName.isEmpty()) {
                                Tag tag = tagService.findOrCreateTag(trimmedTagName);
                                tags.add(tag);
                            }
                        }
                        
                        question.setTags(tags);
                    }
                    
                    return convertToDto(questionRepository.save(question));
                });
    }
    
    public Optional<QuestionDto> acceptAnswer(Long questionId, Long answerId) {
        return questionRepository.findById(questionId)
                .map(question -> {
                    question.setAcceptedAnswerId(answerId);
                    question.setStatus("solved");
                    return convertToDto(questionRepository.save(question));
                });
    }
    
    public boolean deleteQuestion(Long id) {
        if (questionRepository.existsById(id)) {
            questionRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private QuestionDto convertToDto(Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setQuestionId(question.getQuestionId());
        dto.setAuthorId(question.getAuthor().getUserId());
        dto.setAuthorUsername(question.getAuthor().getUsername());
        dto.setTitle(question.getTitle());
        dto.setText(question.getText());
        dto.setImage(question.getImage());
        dto.setStatus(question.getStatus());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setAcceptedAnswerId(question.getAcceptedAnswerId());
        
        // Convert tags to DTOs
        List<TagDto> tagDtos = question.getTags().stream()
                .map(tag -> {
                    TagDto tagDto = new TagDto();
                    tagDto.setTagId(tag.getTagId());
                    tagDto.setName(tag.getName());
                    return tagDto;
                })
                .collect(Collectors.toList());
        dto.setTags(tagDtos);
        
        // Count votes
        List<Vote> votes = voteRepository.findByQuestion(question);
        dto.setUpvotes((int) votes.stream().filter(v -> "upvote".equals(v.getVoteType())).count());
        dto.setDownvotes((int) votes.stream().filter(v -> "downvote".equals(v.getVoteType())).count());
        
        return dto;
    }
    
    public Question findQuestionEntityById(Long id) {
        return questionRepository.findById(id).orElse(null);
    }
} 