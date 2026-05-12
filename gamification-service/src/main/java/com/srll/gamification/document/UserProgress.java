package com.srll.gamification.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "user_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {

    @Id
    private String id;

    @Indexed(unique = true)
    private Long userId;

    @Builder.Default
    private int xp = 0;

    @Builder.Default
    private int level = 1;

    @Builder.Default
    private int totalReviews = 0;

    @Builder.Default
    private int totalCorrect = 0;

    private LocalDate lastReviewDate;

    @Builder.Default
    private List<String> earnedBadges = new ArrayList<>();

    private LocalDateTime updatedAt;
}
