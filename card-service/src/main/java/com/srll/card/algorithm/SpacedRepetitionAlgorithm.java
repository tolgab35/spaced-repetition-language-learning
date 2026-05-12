package com.srll.card.algorithm;

public interface SpacedRepetitionAlgorithm {

    /**
     * @param rating 0-5 quality rating (0-2 = fail, 3-5 = pass)
     */
    ReviewResult calculate(int repetitions, int intervalDays, double easeFactor, int rating);

    record ReviewResult(int newRepetitions, int newIntervalDays, double newEaseFactor) {}
}
