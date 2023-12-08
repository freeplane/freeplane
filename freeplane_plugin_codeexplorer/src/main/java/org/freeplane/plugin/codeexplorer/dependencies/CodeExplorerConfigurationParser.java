/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tngtech.archunit.core.domain.PackageMatcher;

public class CodeExplorerConfigurationParser {
    private static final String CLASS_PATTERN = "[\\w\\.\\|\\(\\)\\*\\[\\]]+";

    private static final String DIRECTION_PATTERN = Pattern.quote(DependencyDirection.UP.notation)
            + "|" + Pattern.quote(DependencyDirection.DOWN.notation)
            + "|" + Pattern.quote(DependencyDirection.ANY.notation);

    private static final Pattern DEPENDENCY_RULE_PATTERN = Pattern.compile("("
            + DependencyVerdict.ALLOWED.keyword + "|"
            + DependencyVerdict.FORBIDDEN.keyword + "|"
            + DependencyVerdict.IGNORED.keyword + ")\\s+"
            + "(" + CLASS_PATTERN + ")\\s*"
            + "(" + DIRECTION_PATTERN + ")"
            + "\\s*("+ CLASS_PATTERN + ")\\s*$");

    private final List<DependencyRule> rules;

    CodeExplorerConfigurationParser(String dsl) {
        List<DependencyRule> rules = new ArrayList<>();
        String[] dslRules = dsl.split("\\n\\s*");

        for (String dslRuleLine : dslRules) {
            String dslRule = dslRuleLine.trim();
            Matcher matcher = DEPENDENCY_RULE_PATTERN.matcher(dslRule);
            if(dslRule.isEmpty() || dslRule.startsWith("#") || dslRule.startsWith("//"))
                continue;
            if (matcher.find()) {
                DependencyVerdict type = DependencyVerdict.parseVerdict(matcher.group(1));
                String originPattern = matcher.group(2);
                String directionNotation = matcher.group(3);
                String targetPattern = matcher.group(4);

                DependencyDirection dependencyDirection = DependencyDirection.parseDirection(directionNotation);

                DependencyRule rule = new DependencyRule(type, originPattern, targetPattern, dependencyDirection);
                rules.add(rule);
            }
            else
                throw new IllegalArgumentException("Invalid rule " + dslRule);

        }
        this.rules = rules;
    }

    public List<DependencyRule> getRules() {
        return rules;
    }
}