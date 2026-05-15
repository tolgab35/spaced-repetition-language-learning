package com.srll.card.repository;

import com.srll.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    List<Card> findByDeckId(Long deckId);

    Optional<Card> findByIdAndDeckUserId(Long id, Long userId);

    @Query("SELECT c FROM Card c WHERE c.deck.userId = :userId AND c.nextReview <= :now ORDER BY c.nextReview")
    List<Card> findDueCards(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    @Query("SELECT c FROM Card c WHERE c.deck.userId = :userId AND c.deck.id = :deckId AND c.nextReview <= :now ORDER BY c.nextReview")
    List<Card> findDueCardsByDeck(@Param("userId") Long userId, @Param("deckId") Long deckId, @Param("now") LocalDateTime now);
}
