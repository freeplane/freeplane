/*
 * Created on 8 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.tngtech.archunit.core.domain.PackageMatcher;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;

class ClassMatcher implements ImportOption{
    private static final Pattern CLASS_LOCATION_PATTERN = Pattern.compile("/[\\w\\$/]+(?=\\.class$)");
    private static final Pattern ANONYMOUS_CLASS_PATTERN = Pattern.compile("\\$\\d+$");
    private final List<PackageMatcher> matchers;
    private final List<String> patterns;

    public ClassMatcher(List<String> patterns) {
        super();
        this.patterns = patterns;
        this.matchers = patterns.stream()
                .map(s -> s.startsWith("..") ? s : ".." + s)
                .map(PackageMatcher::of).collect(Collectors.toList());
    }

    public boolean anyMatch(Location location) {
        String locationString = location.asURI().toString();
        if(locationString.endsWith("/package-info.class"))
            return true;
        Matcher matcher = CLASS_LOCATION_PATTERN.matcher(locationString);
        if (matcher.find()) {
            String fullClassName = matcher.group().replace('/', '.');
            String namedClass;
            Matcher classNameMatcher = ANONYMOUS_CLASS_PATTERN.matcher(fullClassName);
            if(classNameMatcher.find()) {
                namedClass = fullClassName.substring(1, classNameMatcher.start());
            } else
                namedClass =fullClassName.substring(1);
            return anyMatch(namedClass.replace('$', '.'));
        } else
            return false;
    }

    public boolean anyMatch(String name) {
        return matchers.stream().anyMatch(m -> m.matches(name));
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
        ClassMatcher other = (ClassMatcher) obj;
        return Objects.equals(patterns, other.patterns);
    }

    @Override
    public boolean includes(Location location) {
       return matchers.isEmpty() ? true : ! anyMatch(location);
    }
}
