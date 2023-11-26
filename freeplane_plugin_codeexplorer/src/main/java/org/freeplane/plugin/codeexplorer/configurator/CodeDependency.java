/*
 * Created on 23 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.configurator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.freeplane.plugin.codeexplorer.map.ClassNodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

class CodeDependency {
    private static final Pattern ARRAYS = Pattern.compile("\\[+L?([\\w$]+);?");
    private static final Pattern PACKAGES = Pattern.compile("(?<=\\b|\\[L)(?:[a-z0-9_]+\\.)+");
    static Stream<CodeDependency> distinct(Stream<CodeDependency> stream) {
        Map<CodeDependency, CodeDependency> seen = new LinkedHashMap<>();

        stream.forEachOrdered(element ->
        seen.compute(element,
                (key, existing) ->
                existing == null ? element :
                (element.equals(existing) ? existing.dependency.compareTo(element.dependency) < 0 ? existing : element
                        : element
        )));

        return seen.values().stream();
    }

    private final Dependency dependency;
    private final boolean goesUp;
    CodeDependency(Dependency dependendy, boolean goesUp) {
        super();
        this.dependency = dependendy;
        this.goesUp = goesUp;
    }
    public JavaClass getOriginClass() {
        return dependency.getOriginClass();
    }
    public JavaClass getTargetClass() {
        return dependency.getTargetClass();
    }

    public boolean goesUp() {
        return goesUp;
    }

    Dependency getDependency() {
        return dependency;
    }

    public String getDescription() {
        JavaClass originClass = getOriginClass();
        JavaClass targetClass = getTargetClass();
        String description = dependency.getDescription()
                .replace(originClass.getName(), ClassNodeModel.nodeText(originClass))
                .replace(targetClass.getName(), ClassNodeModel.nodeText(targetClass));
        Matcher packagesMatcher = PACKAGES.matcher(description);
        description = packagesMatcher.replaceAll("");

        Matcher arraysMatcher = ARRAYS.matcher(description);
        StringBuffer result = new StringBuffer();

        while (arraysMatcher.find()) {
            arraysMatcher.appendReplacement(result, Matcher.quoteReplacement(convertArrayDescriptor(arraysMatcher.group())));
        }
        arraysMatcher.appendTail(result);

        description = result.toString()
                .replace('$', '.');
        return description;
    }

    static String getDescriptionForComparison(Dependency dependency) {
        return dependency.getDescription()
                .replaceFirst(":\\d+\\)$", ")");
    }

    public boolean descriptionContains(String string) {
        return dependency.getDescription().contains(string);
    }

    private static String convertArrayDescriptor(String descriptor) {
        int arrayDepth = 0;
        for (char ch : descriptor.toCharArray()) {
            if (ch == '[') {
                arrayDepth++;
            } else {
                String type;
                switch (ch) {
                    case 'B': type = "byte"; break;
                    case 'C': type = "char"; break;
                    case 'D': type = "double"; break;
                    case 'F': type = "float"; break;
                    case 'I': type = "int"; break;
                    case 'J': type = "long"; break;
                    case 'S': type = "short"; break;
                    case 'Z': type = "boolean"; break;
                    case 'L':
                        type = descriptor.substring(arrayDepth + 1, descriptor.length() - 1);
                        break;
                    default:
                        return descriptor;
                }

                StringBuilder replacement = new StringBuilder(type);
                for (int i = 0; i < arrayDepth; i++) {
                    replacement.append("[]");
                }
                return replacement.toString();
            }
        }
        return descriptor;
    }
    @Override
    public String toString() {
        return "CodeDependency [dependency=" + dependency + ", goesUp=" + goesUp + "]";
    }
    @Override
    public int hashCode() {
        return dependency.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CodeDependency other = (CodeDependency) obj;
        return Objects.equals(dependency, other.dependency);
    }


}
