package com.srll.javafx.http.dto;

import java.time.LocalDateTime;

public record DeckResponse(Long id, String name, String description,
                           String language, int cardCount, LocalDateTime createdAt) {}
