/*
 * Created on 5 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.core.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LineComparator {

    public static int compareLinesParsingNumbers(String line1, String line2) {
        List<String> segments1 = parseLine(line1.trim());
        List<String> segments2 = parseLine(line2.trim());

        for (int i = 0; i < Math.min(segments1.size(), segments2.size()); i++) {
            String seg1 = segments1.get(i);
            String seg2 = segments2.get(i);
            int result = compareSegments(seg1, seg2);
            if (result != 0) {
                return result;
            }
        }

        return Integer.compare(segments1.size(), segments2.size());
    }

    private static List<String> parseLine(String line) {
        if(line.isEmpty())
            return Collections.emptyList();
        List<String> segments = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();
        boolean isDigitSegment = Character.isDigit(line.charAt(0));

        for (char ch : line.toCharArray()) {
            if (Character.isDigit(ch) == isDigitSegment) {
                currentSegment.append(ch);
            } else {
                segments.add(currentSegment.toString());
                currentSegment = new StringBuilder(String.valueOf(ch));
                isDigitSegment = !isDigitSegment;
            }
        }
        segments.add(currentSegment.toString());

        return segments;
    }

    private static int compareSegments(String seg1, String seg2) {
        if(seg1 == seg2)
            return 0;
        if (Character.isDigit(seg1.charAt(0))&& Character.isDigit(seg2.charAt(0))) {
            try {
                return Long.compare(Long.parseLong(seg1), Long.parseLong(seg2));
            } catch (NumberFormatException e) {
                BigInteger num1 = new BigInteger(seg1);
                BigInteger num2 = new BigInteger(seg2);
                return num1.compareTo(num2);
            }
        }
        int compareResult = seg1.compareTo(seg2);
        if(compareResult == 0)
            return 0;
        int compareIgnoreCaseResult = seg1.compareToIgnoreCase(seg2);
        return compareIgnoreCaseResult != 0 ? compareIgnoreCaseResult : compareResult;
    }
}
