package com.example.main.repository;

import com.example.main.model.Question;
import com.example.main.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByAuthor(User author);
    
    List<Question> findByStatus(String status);
    
    List<Question> findByTitleContainingOrTextContaining(String titleKeyword, String textKeyword);
} 