/*
 * Created on 8 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;

import java.util.Objects;

import com.tngtech.archunit.core.domain.PackageMatcher;

class ClassMatcher {
    private final PackageMatcher matcher;
    private final String pattern;

    public ClassMatcher(String pattern) {
        super();
        this.pattern = pattern;
        this.matcher = PackageMatcher.of(pattern);
    }

    public boolean matches(String aPackage) {
        return matcher.matches(aPackage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassMatcher other = (ClassMatcher) obj;
        return Objects.equals(pattern, other.pattern);
    }
}
