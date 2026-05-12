package com.srll.card.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "review_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewHistory {

    @Id
    private String id;

    @Indexed
    private Long cardId;

    @Indexed
    private Long userId;

    private int rating;

    private int intervalDaysAfter;

    private double easeFactorAfter;

    private LocalDateTime reviewedAt;
}
