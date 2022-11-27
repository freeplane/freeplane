/*
 * Created on 24 Nov 2022
 *
 * author dimitry
 */
package org.freeplane.features.map;

public interface ComparableByPriority extends Comparable<ComparableByPriority>{
    public static final int DEFAULT_PRIORITY = 10;
    default int priority() {return DEFAULT_PRIORITY;}
    @Override
    default int compareTo(ComparableByPriority other) {
        return priority() - other.priority();
    }

}
