/*
 * Created on 3 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.Comparator;
import java.util.Set;
import java.util.function.Function;

class SubgroupComparator {
    static <V> Comparator<Set<V>> comparingByName(Function<V, String> nameFunction) {
        return Comparator.comparing(
            subgroup -> subgroup.stream().map(nameFunction).min(String::compareTo).orElse(""));
    }
}
