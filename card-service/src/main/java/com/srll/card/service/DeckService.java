package com.srll.card.service;

import com.srll.card.dto.DeckRequest;
import com.srll.card.dto.DeckResponse;
import com.srll.card.entity.Deck;
import com.srll.card.repository.DeckRepository;
import com.srll.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;

    public List<DeckResponse> getDecks(Long userId) {
        return deckRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    public DeckResponse getDeck(Long deckId, Long userId) {
        return deckRepository.findByIdAndUserId(deckId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Deck", "id", deckId));
    }

    @Transactional
    public DeckResponse createDeck(DeckRequest request, Long userId) {
        Deck deck = Deck.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .language(request.getLanguage())
                .build();
        return toResponse(deckRepository.save(deck));
    }

    @Transactional
    public DeckResponse updateDeck(Long deckId, DeckRequest request, Long userId) {
        Deck deck = deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck", "id", deckId));
        deck.setName(request.getName());
        deck.setDescription(request.getDescription());
        deck.setLanguage(request.getLanguage());
        return toResponse(deckRepository.save(deck));
    }

    @Transactional
    public void deleteDeck(Long deckId, Long userId) {
        Deck deck = deckRepository.findByIdAndUserId(deckId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck", "id", deckId));
        deckRepository.delete(deck);
    }

    private DeckResponse toResponse(Deck deck) {
        DeckResponse response = new DeckResponse();
        response.setId(deck.getId());
        response.setName(deck.getName());
        response.setDescription(deck.getDescription());
        response.setLanguage(deck.getLanguage());
        response.setCardCount(deck.getCards().size());
        response.setCreatedAt(deck.getCreatedAt());
        return response;
    }
}
