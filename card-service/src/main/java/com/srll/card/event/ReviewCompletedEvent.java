package com.srll.card.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCompletedEvent {
    private Long userId;
    private Long cardId;
    private int rating;
    private boolean passed;
}
