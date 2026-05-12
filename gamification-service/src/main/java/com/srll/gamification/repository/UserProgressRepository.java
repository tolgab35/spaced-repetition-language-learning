package com.srll.gamification.repository;

import com.srll.gamification.document.UserProgress;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserProgressRepository extends MongoRepository<UserProgress, String> {

    Optional<UserProgress> findByUserId(Long userId);
}
