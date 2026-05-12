package com.srll.card.controller;

import com.srll.card.dto.DeckRequest;
import com.srll.card.dto.DeckResponse;
import com.srll.card.service.DeckService;
import com.srll.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/decks")
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeckResponse>>> getDecks(HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.ok(deckService.getDecks(userId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeckResponse>> getDeck(@PathVariable Long id,
                                                              HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.ok(deckService.getDeck(id, userId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeckResponse>> createDeck(@Valid @RequestBody DeckRequest body,
                                                                  HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Deck created", deckService.createDeck(body, userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeckResponse>> updateDeck(@PathVariable Long id,
                                                                  @Valid @RequestBody DeckRequest body,
                                                                  HttpServletRequest request) {
        Long userId = extractUserId(request);
        return ResponseEntity.ok(ApiResponse.ok(deckService.updateDeck(id, body, userId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDeck(@PathVariable Long id,
                                                         HttpServletRequest request) {
        Long userId = extractUserId(request);
        deckService.deleteDeck(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Deck deleted", null));
    }

    private Long extractUserId(HttpServletRequest request) {
        return Long.parseLong(request.getHeader("X-User-Id"));
    }
}
