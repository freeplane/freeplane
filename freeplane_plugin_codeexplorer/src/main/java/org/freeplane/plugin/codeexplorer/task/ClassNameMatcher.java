/*
 * Created on 9 Mar 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import static com.tngtech.archunit.core.domain.PackageMatcher.TO_GROUPS;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.freeplane.plugin.codeexplorer.map.CodeNode;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.PackageMatcher;

class ClassNameMatcher {
    private final PackageMatcher packageMatcher;
    private final Optional<String> name;
    private final boolean ignoresClasses;
    private final boolean matchesSingleClasses;
    private final String pattern;
    ClassNameMatcher(String pattern, boolean ignoresClasses, boolean matchesSingleClasses, Optional<String> name) {
        super();
        this.pattern = pattern;
        this.matchesSingleClasses = matchesSingleClasses;
        this.packageMatcher = PackageMatcher.of(pattern);
        this.name = name;
        this.ignoresClasses = ignoresClasses;
    }

    boolean isIgnored(JavaClass javaClass) {
        return ignoresClasses && packageMatcher.matches(qualifiedClassName(javaClass));
    }

    Optional<String> toGroup(JavaClass javaClass) {
        final Optional<String> joinedNameParts = packageMatcher.match(qualifiedClassName(javaClass)).map(TO_GROUPS)
                .map(List::stream)
                .map(s -> s.collect(Collectors.joining(":")));
        return joinedNameParts
                .map(parts -> name.map(s -> s + " " + parts)
                .orElse(parts));
    }


    private String qualifiedClassName(JavaClass javaClass) {
        if(! matchesSingleClasses)
            return javaClass.getPackageName();
        final String fullName = CodeNode.findEnclosingTopLevelClass(javaClass).getName();
        int lastIndexOfNon$ = fullName.length() - 1;
        while (lastIndexOfNon$ > 0 && fullName.charAt(lastIndexOfNon$) == '$')
            lastIndexOfNon$--;

        return fullName.substring(0, lastIndexOfNon$ + 1);
    }




    public boolean ignoresClasses() {
        return ignoresClasses;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ignoresClasses, name, pattern);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ClassNameMatcher other = (ClassNameMatcher) obj;
        return ignoresClasses == other.ignoresClasses && Objects.equals(name, other.name) && Objects.equals(
                pattern, other.pattern);
    }


}
