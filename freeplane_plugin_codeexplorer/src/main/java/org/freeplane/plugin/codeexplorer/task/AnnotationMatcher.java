/*
 * Created on 5 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.PackageMatcher;

public class AnnotationMatcher {
    private final List<PackageMatcher> matchers;
    private final List<String> patterns;

    public AnnotationMatcher(List<String> patterns) {
        super();
        this.patterns = patterns;
        this.matchers = patterns.stream()
                .map(s -> s.startsWith("..") ? s : ".." + s)
                .map(s -> s.endsWith("()") ? s.substring(0, s.length() - 2) : s)
                .map(PackageMatcher::of).collect(Collectors.toList());
    }

    public boolean matches(JavaAnnotation<?> annotation, String key) {
        if(isEmpty())
            return false;
        String annotationName = annotation.getType().getName().replace('$', '.');
        String annotationNameWithMethod = annotationName + "." + key;
        return IntStream.range(0, patterns.size())
                .anyMatch(i -> matchers.get(i).matches(
                        patterns.get(i).endsWith("()") ? annotationNameWithMethod : annotationName));
    }

    public boolean isEmpty() {
        return patterns.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(patterns);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AnnotationMatcher other = (AnnotationMatcher) obj;
        return Objects.equals(patterns, other.patterns);
    }

}
