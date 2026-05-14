package com.srll.javafx.http.dto;

import java.time.LocalDateTime;

public record CardResponse(Long id, Long deckId, String front, String back,
                           int intervalDays, int repetitions, double easeFactor,
                           LocalDateTime nextReview) {}
