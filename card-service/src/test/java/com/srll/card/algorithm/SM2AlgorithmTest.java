package com.srll.card.algorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SM2AlgorithmTest {

    private SM2Algorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new SM2Algorithm();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void failedRecall_resetsRepetitions(int rating) {
        SpacedRepetitionAlgorithm.ReviewResult result = algorithm.calculate(5, 21, 2.5, rating);

        assertThat(result.newRepetitions()).isEqualTo(0);
        assertThat(result.newIntervalDays()).isEqualTo(1);
    }

    @Test
    void firstSuccessfulReview_givesInterval1() {
        SpacedRepetitionAlgorithm.ReviewResult result = algorithm.calculate(0, 1, 2.5, 4);

        assertThat(result.newRepetitions()).isEqualTo(1);
        assertThat(result.newIntervalDays()).isEqualTo(1);
    }

    @Test
    void secondSuccessfulReview_givesInterval6() {
        SpacedRepetitionAlgorithm.ReviewResult result = algorithm.calculate(1, 1, 2.5, 4);

        assertThat(result.newRepetitions()).isEqualTo(2);
        assertThat(result.newIntervalDays()).isEqualTo(6);
    }

    @Test
    void thirdReview_multipliesIntervalByEaseFactor() {
        SpacedRepetitionAlgorithm.ReviewResult result = algorithm.calculate(2, 6, 2.5, 4);

        assertThat(result.newIntervalDays()).isEqualTo(15); // round(6 * 2.5)
    }

    @Test
    void easeFactorDecreasesOnHardRating() {
        SpacedRepetitionAlgorithm.ReviewResult result = algorithm.calculate(3, 6, 2.5, 3);

        assertThat(result.newEaseFactor()).isLessThan(2.5);
    }

    @Test
    void easeFactorIncreasesOnEasyRating() {
        SpacedRepetitionAlgorithm.ReviewResult result = algorithm.calculate(3, 6, 2.5, 5);

        assertThat(result.newEaseFactor()).isGreaterThan(2.5);
    }

    @Test
    void easeFactorNeverDropsBelowMinimum() {
        SpacedRepetitionAlgorithm.ReviewResult result = algorithm.calculate(0, 1, 1.3, 0);

        assertThat(result.newEaseFactor()).isGreaterThanOrEqualTo(1.3);
    }
}
