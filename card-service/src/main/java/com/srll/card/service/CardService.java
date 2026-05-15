package com.srll.card.service;

import com.srll.card.algorithm.SpacedRepetitionAlgorithm;
import com.srll.card.document.ReviewHistory;
import com.srll.card.dto.CardRequest;
import com.srll.card.dto.CardResponse;
import com.srll.card.dto.ReviewRequest;
import com.srll.card.entity.Card;
import com.srll.card.entity.Deck;
import com.srll.card.event.ReviewCompletedEvent;
import com.srll.card.repository.CardRepository;
import com.srll.card.repository.DeckRepository;
import com.srll.card.repository.ReviewHistoryRepository;
import com.srll.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final DeckRepository deckRepository;
    private final ReviewHistoryRepository reviewHistoryRepository;
    private final SpacedRepetitionAlgorithm algorithm;
    private final KafkaTemplate<String, ReviewCompletedEvent> kafkaTemplate;

    private static final String REVIEW_TOPIC = "review-completed";

    public List<CardResponse> getCards(Long deckId) {
        return cardRepository.findByDeckId(deckId).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CardResponse> getDueCards(Long userId) {
        return cardRepository.findDueCards(userId, LocalDateTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    public List<CardResponse> getDueCards(Long userId, Long deckId) {
        return cardRepository.findDueCardsByDeck(userId, deckId, LocalDateTime.now()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CardResponse createCard(Long deckId, CardRequest request, Long userId) {
        Deck deck = deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck", "id", deckId));

        Card card = Card.builder()
                .deck(deck)
                .front(request.getFront())
                .back(request.getBack())
                .build();

        return toResponse(cardRepository.save(card));
    }

    @Transactional
    public CardResponse updateCard(Long cardId, CardRequest request, Long userId) {
        Card card = cardRepository.findByIdAndDeckUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId));
        card.setFront(request.getFront());
        card.setBack(request.getBack());
        return toResponse(cardRepository.save(card));
    }

    @Transactional
    public void deleteCard(Long cardId, Long userId) {
        Card card = cardRepository.findByIdAndDeckUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId));
        cardRepository.delete(card);
    }

    @Transactional
    public CardResponse review(Long cardId, ReviewRequest request, Long userId) {
        Card card = cardRepository.findByIdAndDeckUserId(cardId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Card", "id", cardId));

        SpacedRepetitionAlgorithm.ReviewResult result = algorithm.calculate(
                card.getRepetitions(), card.getIntervalDays(), card.getEaseFactor(), request.getRating());

        card.setRepetitions(result.newRepetitions());
        card.setIntervalDays(result.newIntervalDays());
        card.setEaseFactor(result.newEaseFactor());
        card.setNextReview(LocalDateTime.now().plusDays(result.newIntervalDays()));

        cardRepository.save(card);

        ReviewHistory history = ReviewHistory.builder()
                .cardId(cardId)
                .userId(userId)
                .rating(request.getRating())
                .intervalDaysAfter(result.newIntervalDays())
                .easeFactorAfter(result.newEaseFactor())
                .reviewedAt(LocalDateTime.now())
                .build();
        reviewHistoryRepository.save(history);

        kafkaTemplate.send(REVIEW_TOPIC, String.valueOf(userId),
                new ReviewCompletedEvent(userId, cardId, request.getRating(), request.getRating() >= 3));

        return toResponse(card);
    }

    private CardResponse toResponse(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setDeckId(card.getDeck().getId());
        response.setFront(card.getFront());
        response.setBack(card.getBack());
        response.setIntervalDays(card.getIntervalDays());
        response.setRepetitions(card.getRepetitions());
        response.setEaseFactor(card.getEaseFactor());
        response.setNextReview(card.getNextReview());
        return response;
    }
}
