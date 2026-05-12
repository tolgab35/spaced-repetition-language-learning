package com.srll.card.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeckResponse {
    private Long id;
    private String name;
    private String description;
    private String language;
    private int cardCount;
    private LocalDateTime createdAt;
}
