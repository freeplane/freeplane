/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.freeplane.plugin.codeexplorer.dependencies.DependencyDirection;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyRule;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyVerdict;

import com.tngtech.archunit.core.importer.ImportOption;

public class ParsedConfiguration {
    public static final String HELP = "Rule Format:\n"
            + "-> Rules are defined one per line in the following formats:\n"
            + "\n"
            + "  [command] [originPattern] [direction] [targetPattern]\n"
            + "  classpath [path]\n"
            + "  ignore class [classPattern]\n"
            + "  import annotation [classPattern]\n"
            + "  import annotation [classPattern].[methodName]()\n"
            + "\n"
            + "-> Commands (Related to Dependency Rules): allow, forbid, ignore\n"
            + "   - 'allow', 'forbid', 'ignore' commands are used to define rules for managing dependencies between different parts of the code.\n"
            + "-> Direction: ->, ->v, ->^ (representing bidirectional, downward, upward respectively)\n"
            + "   - Specifies the direction of dependency between the origin and target patterns.\n"
            + "-> Patterns: follow AspectJ-like syntax for matching package names\n"
            + "-> Path: additional path segment to be appended to each root directory defined in the 'locations' table.\n"
            + "-> ClassPattern: same syntax as originPattern for matching class names\n"
            + "   - Note: For 'ignore class', '..' is implicitly added at the start of the pattern if not already present, ensuring a broader match.\n"
            + "\n"
            + "Locations Table:\n"
            + "-> The 'locations' table defines the root directories for the project.\n"
            + "-> The 'classpath' lines specify additional path segments to be appended to these root directories, resulting in paths like '/root/target/classes'.\n"
            + "\n"
            + "Default Classpath Behavior:\n"
            + "-> If no 'classpath' elements are given:\n"
            + "   - If the location directory contains 'pom.xml', defaults to appending 'target/classes'.\n"
            + "   - If the location directory contains 'build.gradle', defaults to appending 'build/classes'.\n"
            + "   - Otherwise, defaults to appending the current directory ('.').\n"
            + "\n"
            + "Comments:\n"
            + "-> Lines starting with '#' or '//' are considered comments and ignored.\n"
            + "\n"
            + "Examples:\n"
            + "\n"
            + "  allow *.service.* -> *.repository.*\n"
            + "  forbid *.*.controller*.. ->^ ..model..\n"
            + "  ignore ..util.. ->v ..*Helper..\n"
            + "  classpath /target/classes\n"
            + "  ignore class com.example..*ServiceImpl..\n"
            + "  import annotation com.example..*Annotation.*.value()\n"
            + "\n"
            + "Note:\n"
            + "-> 'classpath' lines augment the root directories defined in the 'locations' table.\n"
            + "-> The 'ignore class' command is designed to include a broader range of classes by implicitly adding '..' at the start of the pattern.\n"
            + "-> The commands 'allow', 'forbid', and 'ignore' specifically dictate how different code segments (e.g., packages, classes) can depend on each other.\n"
            + "";

     private static final String CLASS_PATTERN = "[\\w\\.\\|\\(\\)\\*\\[\\]]+";

    private static final String DIRECTION_PATTERN = Pattern.quote(DependencyDirection.UP.notation)
            + "|" + Pattern.quote(DependencyDirection.DOWN.notation)
            + "|" + Pattern.quote(DependencyDirection.ANY.notation);

    private static final Pattern DEPENDENCY_RULE_PATTERN = Pattern.compile(
            "^\\s*(" + DependencyVerdict.ALLOWED.keyword + "|"
            + DependencyVerdict.FORBIDDEN.keyword + "|"
            + DependencyVerdict.IGNORED.keyword + ")\\s+"
            + "(" + CLASS_PATTERN + ")\\s*"
            + "(" + DIRECTION_PATTERN + ")"
            + "\\s*("+ CLASS_PATTERN + ")\\s*$");

    private static final Pattern CLASSPATH_PATTERN = Pattern.compile(
            "^\\s*classpath\\s+/*(.*\\S)\\s*$");

    private static final Pattern IGNORED_CLASS_PATTERN = Pattern.compile(
            "^\\s*ignore\\s+class\\s+(" + CLASS_PATTERN + ")\\s*$");

    private static final Pattern IMPORTED_ANNOTATION_PATTERN = Pattern.compile(
            "^\\s*import\\s+annotation\\s+(" + CLASS_PATTERN + ")\\s*$");

    private final List<DependencyRule> rules;
    private final ClassMatcher ignoredClasses;
    private final AnnotationMatcher annotationMatcher;
    private final List<String> subpaths;



    public ParsedConfiguration(String dsl) {
        List<DependencyRule> dependencyRules = new ArrayList<>();
        List<String> ignoredClasses = new ArrayList<>();
        List<String> importedAnnotations = new ArrayList<>();
        List<String> subpaths = new ArrayList<>();
        String[] dslRules = dsl.split("\\n\\s*");

        for (String dslRuleLine : dslRules) {
            String dslRule = dslRuleLine.trim();
            if(dslRule.isEmpty() || dslRule.startsWith("#") || dslRule.startsWith("//"))
                continue;
            Matcher dependencyMatcher = DEPENDENCY_RULE_PATTERN.matcher(dslRule);
            if (dependencyMatcher.find()) {
                DependencyVerdict type = DependencyVerdict.parseVerdict(dependencyMatcher.group(1));
                String originPattern = dependencyMatcher.group(2);
                String directionNotation = dependencyMatcher.group(3);
                String targetPattern = dependencyMatcher.group(4);

                DependencyDirection dependencyDirection = DependencyDirection.parseDirection(directionNotation);

                DependencyRule rule = new DependencyRule(type, originPattern, targetPattern, dependencyDirection);
                dependencyRules.add(rule);
            } else {
                Matcher classpathMatcher = CLASSPATH_PATTERN.matcher(dslRule);
                if (classpathMatcher.find()) {
                    subpaths.add(classpathMatcher.group(1));
                } else {
                    Matcher ignoredClassMatcher = IGNORED_CLASS_PATTERN.matcher(dslRule);
                    if (ignoredClassMatcher.find()) {
                        ignoredClasses.add(ignoredClassMatcher.group(1));
                    } else {
                        Matcher importedAnnotationMatcher = IMPORTED_ANNOTATION_PATTERN.matcher(dslRule);
                        if(importedAnnotationMatcher.find()) {
                            importedAnnotations.add(importedAnnotationMatcher.group(1));
                        }
                        else
                            throw new IllegalArgumentException("Invalid rule " + dslRule);
                    }
                }
            }

        }
        this.rules = dependencyRules;
        this.ignoredClasses = new ClassMatcher(ignoredClasses);
        this.annotationMatcher = new AnnotationMatcher(importedAnnotations);
        this.subpaths = subpaths;
    }

    public DependencyJudge judge() {
        return new DependencyJudge(rules);
    }

    public AnnotationMatcher annotationMatcher() {
        return annotationMatcher;
    }

    public DirectoryMatcher directoryMatcher(Collection<File> locations) {
        return new DirectoryMatcher(locations, subpaths);
    }

    public ImportOption importOption() {
        return ignoredClasses;
    }

    public ConfigurationChange configurationChange(ParsedConfiguration previousConfiguration) {
        if(previousConfiguration == null
                || ! subpaths.equals(previousConfiguration.subpaths)
                || ! ignoredClasses.equals(previousConfiguration.ignoredClasses))
            return ConfigurationChange.CODE_BASE;
        if(! rules.equals(previousConfiguration.rules)
                || ! annotationMatcher.equals(previousConfiguration.annotationMatcher))
                return ConfigurationChange.CONFIGURATION;
        return ConfigurationChange.SAME;
    }
}