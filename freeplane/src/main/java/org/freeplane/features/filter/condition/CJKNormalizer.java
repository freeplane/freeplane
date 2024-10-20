/*
 * Created on 20 Oct 2024
 *
 * author dimitry
 */
package org.freeplane.features.filter.condition;

import java.util.regex.Pattern;

/**
 * A utility class for normalizing CJK (Chinese, Japanese, Korean) text by removing
 * unnecessary spaces between CJK characters.
 */
public class CJKNormalizer {

    /**
     * Unicode range covering scripts for Chinese, Japanese, Korean (CJK),
     * and some Southeast Asian languages, plus full-width characters.
     * CJK refers to Chinese (Han characters), Japanese (Kanji, Hiragana, Katakana),
     * and Korean (Hangul, Hanja), which share many characters in Unicode.
     */
    private static final String UNICODE_CJK_AND_FULLWIDTH_RANGES = "[" +
            "\u3040-\u30FF" +   // Hiragana + Katakana
            "\u4E00-\u9FFF" +   // Kanji (CJK Unified Ideographs) + Hanja
            "\u1100-\u11FF" +   // Hangul Jamo (Korean)
            "\uAC00-\uD7AF" +   // Hangul Syllables (Korean)
            "\u0E00-\u0E7F" +   // Thai
            "\u0E80-\u0EFF" +   // Lao
            "\u1780-\u17FF" +   // Khmer
            "\u1000-\u109F" +   // Burmese
            "\u3000-\u303F" +   // Full-width punctuation
            "\uFF00-\uFFEF" +   // Full-width Roman characters and symbols
            "]";

    /**
     * A compiled pattern to match any character within the specified Unicode ranges for
     * CJK characters and full-width symbols.
     */
    private static final Pattern CJK_FULLWIDTH_CHARACTERS_PATTERN = Pattern.compile(UNICODE_CJK_AND_FULLWIDTH_RANGES);

    /**
     * A compiled pattern to match spaces that appear between two CJK or full-width characters
     * as defined by the specified Unicode ranges.
     */
    private static final Pattern WHITESPACE_BETWEEN_CJK_PATTERN = Pattern.compile(
            "(?<=" + UNICODE_CJK_AND_FULLWIDTH_RANGES + ")\\s+(?=" + UNICODE_CJK_AND_FULLWIDTH_RANGES + ")");

    /**
     * A simple compiled pattern to match any whitespace characters.
     */
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");

    /**
     * Removes spaces between CJK (Chinese, Japanese, Korean) characters and full-width Roman characters
     * if both CJK characters and whitespace are found in the input string.
     *
     * @param value The input string potentially containing CJK characters and whitespace.
     * @return A new string with spaces between CJK characters removed, or the original string
     *         if no CJK characters or whitespace are found.
     */
    public static String removeSpacesBetweenCJKCharacters(String value) {
        if(CJK_FULLWIDTH_CHARACTERS_PATTERN.matcher(value).find() && WHITESPACE_PATTERN.matcher(value).find())
            return WHITESPACE_BETWEEN_CJK_PATTERN.matcher(value).replaceAll("");
        else
            return value;
    }
}