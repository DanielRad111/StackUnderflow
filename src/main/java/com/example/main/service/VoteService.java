package com.example.main.service;

import com.example.main.dto.VoteDto;
import com.example.main.model.Answer;
import com.example.main.model.Question;
import com.example.main.model.User;
import com.example.main.model.Vote;
import com.example.main.repository.VoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VoteService {
    
    @Autowired
    private VoteRepository voteRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private AnswerService answerService;
    
    // Score constants
    private static final float QUESTION_UPVOTE_SCORE = 2.5f;
    private static final float QUESTION_DOWNVOTE_SCORE = -1.5f;
    private static final float ANSWER_UPVOTE_SCORE = 5.0f;
    private static final float ANSWER_DOWNVOTE_SCORE = -2.5f;
    private static final float DOWNVOTE_PENALTY = -1.5f;
    
    public List<VoteDto> getAllVotes() {
        return voteRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public Optional<VoteDto> getVoteById(Long id) {
        return voteRepository.findById(id)
                .map(this::convertToDto);
    }
    
    public List<VoteDto> getVotesByUser(Long userId) {
        User user = userService.findUserEntityById(userId);
        if (user == null) {
            return List.of();
        }
        return voteRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    public VoteDto voteOnQuestion(Long userId, Long questionId, String voteType) {
        User user = userService.findUserEntityById(userId);
        Question question = questionService.findQuestionEntityById(questionId);
        
        if (user == null || question == null) {
            return null;
        }
        
        // Users cannot vote on their own questions
        if (question.getAuthor().getUserId().equals(userId)) {
            return null;
        }
        
        // Check if user already voted on this question
        Optional<Vote> existingVote = voteRepository.findByUserAndQuestion(user, question);
        
        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            // If same vote type, remove the vote (toggle)
            if (vote.getVoteType().equals(voteType)) {
                // Revert score changes
                if ("upvote".equals(voteType)) {
                    userService.updateScore(question.getAuthor().getUserId(), -QUESTION_UPVOTE_SCORE);
                } else {
                    userService.updateScore(question.getAuthor().getUserId(), -QUESTION_DOWNVOTE_SCORE);
                    userService.updateScore(userId, -DOWNVOTE_PENALTY); // Refund the downvote penalty
                }
                
                voteRepository.delete(vote);
                return null;
            } else {
                // Change vote type - need to update scores accordingly
                String oldVoteType = vote.getVoteType();
                vote.setVoteType(voteType);
                
                // Update author's score
                if ("upvote".equals(oldVoteType) && "downvote".equals(voteType)) {
                    // Changed from upvote to downvote
                    userService.updateScore(question.getAuthor().getUserId(), -QUESTION_UPVOTE_SCORE + QUESTION_DOWNVOTE_SCORE);
                    userService.updateScore(userId, DOWNVOTE_PENALTY); // Apply downvote penalty
                } else {
                    // Changed from downvote to upvote
                    userService.updateScore(question.getAuthor().getUserId(), -QUESTION_DOWNVOTE_SCORE + QUESTION_UPVOTE_SCORE);
                    userService.updateScore(userId, -DOWNVOTE_PENALTY); // Refund the downvote penalty
                }
                
                return convertToDto(voteRepository.save(vote));
            }
        } else {
            // Create new vote
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setQuestion(question);
            vote.setVoteType(voteType);
            
            // Update scores
            if ("upvote".equals(voteType)) {
                userService.updateScore(question.getAuthor().getUserId(), QUESTION_UPVOTE_SCORE);
            } else {
                userService.updateScore(question.getAuthor().getUserId(), QUESTION_DOWNVOTE_SCORE);
                userService.updateScore(userId, DOWNVOTE_PENALTY); // Apply downvote penalty
            }
            
            return convertToDto(voteRepository.save(vote));
        }
    }
    
    public VoteDto voteOnAnswer(Long userId, Long answerId, String voteType) {
        User user = userService.findUserEntityById(userId);
        Answer answer = answerService.findAnswerEntityById(answerId);
        
        if (user == null || answer == null) {
            return null;
        }
        
        // Users cannot vote on their own answers
        if (answer.getAuthor().getUserId().equals(userId)) {
            return null;
        }
        
        // Check if user already voted on this answer
        Optional<Vote> existingVote = voteRepository.findByUserAndAnswer(user, answer);
        
        if (existingVote.isPresent()) {
            Vote vote = existingVote.get();
            // If same vote type, remove the vote (toggle)
            if (vote.getVoteType().equals(voteType)) {
                // Revert score changes
                if ("upvote".equals(voteType)) {
                    userService.updateScore(answer.getAuthor().getUserId(), -ANSWER_UPVOTE_SCORE);
                } else {
                    userService.updateScore(answer.getAuthor().getUserId(), -ANSWER_DOWNVOTE_SCORE);
                    userService.updateScore(userId, -DOWNVOTE_PENALTY); // Refund the downvote penalty
                }
                
                voteRepository.delete(vote);
                return null;
            } else {
                // Change vote type - need to update scores accordingly
                String oldVoteType = vote.getVoteType();
                vote.setVoteType(voteType);
                
                // Update author's score
                if ("upvote".equals(oldVoteType) && "downvote".equals(voteType)) {
                    // Changed from upvote to downvote
                    userService.updateScore(answer.getAuthor().getUserId(), -ANSWER_UPVOTE_SCORE + ANSWER_DOWNVOTE_SCORE);
                    userService.updateScore(userId, DOWNVOTE_PENALTY); // Apply downvote penalty
                } else {
                    // Changed from downvote to upvote
                    userService.updateScore(answer.getAuthor().getUserId(), -ANSWER_DOWNVOTE_SCORE + ANSWER_UPVOTE_SCORE);
                    userService.updateScore(userId, -DOWNVOTE_PENALTY); // Refund the downvote penalty
                }
                
                return convertToDto(voteRepository.save(vote));
            }
        } else {
            // Create new vote
            Vote vote = new Vote();
            vote.setUser(user);
            vote.setAnswer(answer);
            vote.setVoteType(voteType);
            
            // Update scores
            if ("upvote".equals(voteType)) {
                userService.updateScore(answer.getAuthor().getUserId(), ANSWER_UPVOTE_SCORE);
            } else {
                userService.updateScore(answer.getAuthor().getUserId(), ANSWER_DOWNVOTE_SCORE);
                userService.updateScore(userId, DOWNVOTE_PENALTY); // Apply downvote penalty
            }
            
            return convertToDto(voteRepository.save(vote));
        }
    }
    
    public boolean deleteVote(Long id) {
        Optional<Vote> voteOpt = voteRepository.findById(id);
        if (voteOpt.isPresent()) {
            Vote vote = voteOpt.get();
            
            // Revert score changes
            if (vote.getQuestion() != null) {
                if ("upvote".equals(vote.getVoteType())) {
                    userService.updateScore(vote.getQuestion().getAuthor().getUserId(), -QUESTION_UPVOTE_SCORE);
                } else {
                    userService.updateScore(vote.getQuestion().getAuthor().getUserId(), -QUESTION_DOWNVOTE_SCORE);
                    userService.updateScore(vote.getUser().getUserId(), -DOWNVOTE_PENALTY); // Refund the downvote penalty
                }
            } else if (vote.getAnswer() != null) {
                if ("upvote".equals(vote.getVoteType())) {
                    userService.updateScore(vote.getAnswer().getAuthor().getUserId(), -ANSWER_UPVOTE_SCORE);
                } else {
                    userService.updateScore(vote.getAnswer().getAuthor().getUserId(), -ANSWER_DOWNVOTE_SCORE);
                    userService.updateScore(vote.getUser().getUserId(), -DOWNVOTE_PENALTY); // Refund the downvote penalty
                }
            }
            
            voteRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private VoteDto convertToDto(Vote vote) {
        VoteDto dto = new VoteDto();
        dto.setVoteId(vote.getVoteId());
        dto.setUserId(vote.getUser().getUserId());
        
        if (vote.getQuestion() != null) {
            dto.setQuestionId(vote.getQuestion().getQuestionId());
        }
        
        if (vote.getAnswer() != null) {
            dto.setAnswerId(vote.getAnswer().getAnswerId());
        }
        
        dto.setVoteType(vote.getVoteType());
        return dto;
    }
} 