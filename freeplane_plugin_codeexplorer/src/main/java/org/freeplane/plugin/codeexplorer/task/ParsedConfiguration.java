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
            "^\\s*classpath\\s+" + "(.*\\S)\\s*$");

    private static final Pattern IGNORED_CLASS_PATTERN = Pattern.compile(
            "^\\s*ignore\\s+class\\s+(" + CLASS_PATTERN + ")\\s*$");

    private final List<DependencyRule> rules;
    private final ClassMatcher ignoredClasses;
    private final List<String> subpaths;


    public ParsedConfiguration(String dsl) {
        List<DependencyRule> dependencyRules = new ArrayList<>();
        List<String> ignoredClasses = new ArrayList<>();
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
                    } else
                        throw new IllegalArgumentException("Invalid rule " + dslRule);
                }
            }

        }
        this.rules = dependencyRules;
        this.ignoredClasses = new ClassMatcher(ignoredClasses);
        this.subpaths = subpaths;
    }

    public DependencyJudge judge() {
        return new DependencyJudge(rules);
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
        if(! rules.equals(previousConfiguration.rules))
                return ConfigurationChange.JUDGE;
        return ConfigurationChange.SAME;
    }
}