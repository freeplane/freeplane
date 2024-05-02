/*
 * Created on 2 May 2024
 *
 * author dimitry
 */
package org.freeplane.features.filter;
import static org.assertj.core.api.Assertions.assertThat;

import org.freeplane.features.filter.StringMatchingStrategy.Type;
import org.junit.Test;

public class PseudoDamerauLevenshteinTest {

    @Test
    public void testGlobalMatching() {
        PseudoDamerauLevenshtein matcher = new PseudoDamerauLevenshtein();
        matcher.init("file", "file", Type.ALL);
        assertThat(matcher.distance())
            .as("Distance for identical strings should be 0.")
            .isEqualTo(0);

        matcher.init("test", "tset", Type.ALL);
        assertThat(matcher.distance())
            .as("Distance for one transposition should be 1.")
            .isEqualTo(1);

        matcher.init("sail", "failing", Type.ALL);
        assertThat(matcher.distance())
            .as("Distance for non-substring global match.")
            .isEqualTo(4);
    }

    @Test
    public void testSubstringMatching() {
        PseudoDamerauLevenshtein matcher = new PseudoDamerauLevenshtein();
        matcher.init("test", "this is a test string", Type.SUBSTRING);
        assertThat(matcher.distance())
            .as("Distance for perfect substring match should be 0.")
            .isEqualTo(0);

        matcher.init("Java", "I love JavaScript", Type.SUBSTRING);
        assertThat(matcher.distance())
            .as("Distance should be low as 'Java' is a substring of 'JavaScript'.")
            .isEqualTo(0);
    }

    @Test
    public void testWordWiseMatching() {
        PseudoDamerauLevenshtein matcher = new PseudoDamerauLevenshtein();
        matcher.init("file", "a file is a file", Type.WORDWISE);
        assertThat(matcher.distance())
            .as("Distance at word boundaries should be 0.")
            .isEqualTo(0);

        matcher.init("test", "testing is fun", Type.WORDWISE);
        assertThat(matcher.distance())
            .as("Distance since 'test' and 'testing' only match at word start.")
            .isEqualTo(3);  // Verifies that the distance is not 0, exact value depends on your logic


        matcher.init("test", "attest is fun", Type.WORDWISE);
        assertThat(matcher.distance())
            .as("Distance since 'test' and 'testing' only match at word start.")
            .isEqualTo(2);  // Verifies that the distance is not 0, exact value depends on your logic

        matcher.init("fun", "method or fun", Type.WORDWISE);
        assertThat(matcher.distance())
            .as("Distance since 'fun' appears as a whole word.")
            .isEqualTo(0);
    }
}
