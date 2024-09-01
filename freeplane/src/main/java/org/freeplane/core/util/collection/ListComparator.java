/*
 * Created on 10 May 2024
 *
 * author dimitry
 */
package org.freeplane.core.util.collection;

import java.util.List;

public class ListComparator {
    public static <T extends Comparable<T>> int compareLists(List<T> list1, List<T> list2) {
        if (list1 == null || list2 == null) {
            if (list1 == null && list2 == null) return 0;
            return list1 == null ? -1 : 1;
        }
        int minLength = Math.min(list1.size(), list2.size());
        for (int i = 0; i < minLength; i++) {
            int comparisonResult = list1.get(i).compareTo(list2.get(i));
            if (comparisonResult != 0) {
                return Integer.compare(comparisonResult, 0);
            }
        }
        return Integer.compare(list1.size(), list2.size());
    }
}
