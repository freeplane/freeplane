/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;

/**
 * Enums for specifying dependency rule types.
 */
public enum DependencyVerdict {
    ALLOWED("allow"), FORBIDDEN("forbid"), IGNORED("ignore");

    public static DependencyVerdict parseVerdict(String keyword) {
        for(DependencyVerdict verdict : DependencyVerdict.values())
            if(keyword.equals(verdict.keyword))
                return verdict;
        throw new IllegalArgumentException(
                "No enum constant for notation " + keyword);

    }

    public final String keyword;

    DependencyVerdict(String keyword) {
        this.keyword = keyword;
    }
}