package com.srll.card.repository;

import com.srll.card.document.ReviewHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewHistoryRepository extends MongoRepository<ReviewHistory, String> {

    List<ReviewHistory> findByCardIdOrderByReviewedAtDesc(Long cardId);

    List<ReviewHistory> findByUserIdOrderByReviewedAtDesc(Long userId);
}
