/*
 * Created on 23 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.plugin.codeexplorer.map.ClassNode;
import org.freeplane.plugin.codeexplorer.map.CodeNode;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;

public class CodeDependency {
    private static final Pattern ARRAYS = Pattern.compile("\\[+L?([\\w$]+);?");
    private static final Pattern PACKAGES = Pattern.compile("(?<=\\b|\\[L)(?:[a-z0-9_]+\\.)+");

    private final Dependency dependency;
    private final boolean goesUp;
    private final DependencyVerdict dependencyVerdict;
    public CodeDependency(Dependency dependendy, boolean goesUp, DependencyVerdict dependencyVerdict) {
        super();
        this.dependency = dependendy;
        this.goesUp = goesUp;
        this.dependencyVerdict = dependencyVerdict;
    }
    public JavaClass getOriginClass() {
        return dependency.getOriginClass();
    }
    public JavaClass getTargetClass() {
        return dependency.getTargetClass();
    }

    public DependencyVerdict dependencyVerdict() {
        return dependencyVerdict;
    }

    public boolean goesUp() {
        return goesUp;
    }

    public Dependency getDependency() {
        return dependency;
    }

    public String getDescription() {
        JavaClass originClass = getOriginClass();
        JavaClass targetClass = getTargetClass();
        JavaClass namedOrigin = CodeNode.findEnclosingNamedClass(originClass);
        JavaClass namedTarget = CodeNode.findEnclosingNamedClass(targetClass);
        String description = dependency.getDescription()
                .replace(namedOrigin.getName(), ClassNode.getSimpleName(namedOrigin))
                .replace(namedTarget.getName(), ClassNode.getSimpleName(namedTarget));
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

    public String describeVerdict() {
        return dependencyVerdict.name().toLowerCase() + " (goes " +  (goesUp ? "up" : "down") + ")";
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
