package com.srll.card.algorithm;

import org.springframework.stereotype.Component;

/**
 * SuperMemo SM-2 spaced repetition algorithm.
 * https://www.supermemo.com/en/archives1990-2015/english/ol/sm2
 */
@Component
public class SM2Algorithm implements SpacedRepetitionAlgorithm {

    private static final double MIN_EASE_FACTOR = 1.3;

    @Override
    public ReviewResult calculate(int repetitions, int intervalDays, double easeFactor, int rating) {
        if (rating < 3) {
            // Failed recall — reset to beginning
            return new ReviewResult(0, 1, Math.max(MIN_EASE_FACTOR, easeFactor - 0.2));
        }

        double newEaseFactor = easeFactor + (0.1 - (5 - rating) * (0.08 + (5 - rating) * 0.02));
        newEaseFactor = Math.max(MIN_EASE_FACTOR, newEaseFactor);

        int newRepetitions = repetitions + 1;
        int newInterval;

        if (repetitions == 0) {
            newInterval = 1;
        } else if (repetitions == 1) {
            newInterval = 6;
        } else {
            newInterval = (int) Math.round(intervalDays * easeFactor);
        }

        return new ReviewResult(newRepetitions, newInterval, newEaseFactor);
    }
}
