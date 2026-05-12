package com.srll.card.repository;

import com.srll.card.entity.Deck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeckRepository extends JpaRepository<Deck, Long> {

    List<Deck> findByUserId(Long userId);

    Optional<Deck> findByIdAndUserId(Long id, Long userId);
}
