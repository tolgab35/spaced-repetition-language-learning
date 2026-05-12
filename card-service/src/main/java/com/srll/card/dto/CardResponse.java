package com.srll.card.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CardResponse {
    private Long id;
    private Long deckId;
    private String front;
    private String back;
    private int intervalDays;
    private int repetitions;
    private double easeFactor;
    private LocalDateTime nextReview;
}
