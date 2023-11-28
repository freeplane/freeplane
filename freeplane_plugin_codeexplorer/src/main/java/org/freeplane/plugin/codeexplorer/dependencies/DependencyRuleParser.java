/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import com.tngtech.archunit.core.domain.PackageMatcher;

class DependencyRuleParser {

    static List<DependencyRule> parseDSL(String dsl) {
        List<DependencyRule> rules = new ArrayList<>();
        String[] dslRules = dsl.split("\\n\\s*");

        for (String dslRule : dslRules) {
            Matcher matcher = DependencyJudge.DEPENDENCY_RULE_PATTERN.matcher(dslRule.trim());
            if (matcher.find()) {
                DependencyVerdict type = DependencyVerdict.parseVerdict(matcher.group(1));
                String originPattern = matcher.group(2);
                String directionNotation = matcher.group(3);
                String targetPattern = matcher.group(4);

                PackageMatcher originMatcher = PackageMatcher.of(originPattern);
                PackageMatcher targetMatcher = PackageMatcher.of(targetPattern);
                DependencyDirection dependencyDirection = DependencyDirection.parseDirection(directionNotation);

                DependencyRule rule = new DependencyRule(type, originMatcher, targetMatcher, dependencyDirection);
                rules.add(rule);
            }
        }
        return rules;
    }
}