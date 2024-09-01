/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.freeplane.plugin.codeexplorer.dependencies.DependencyRule;
import org.freeplane.plugin.codeexplorer.dependencies.DependencyVerdict;

import com.tngtech.archunit.core.domain.Dependency;

/**
 * DependencyMatcher is used to parse and apply rules for dependencies between Java classes.
 * It utilizes a domain-specific language (DSL) to define allowed, forbidden, or ignored dependencies.
 *
 * DSL Format:
 * -> Rules are defined in the format: [command] [originPattern] [direction] [targetPattern]
 * -> Commands: allow, forbid, ignore
 * -> Direction: ->^v, ->v, ->^ (representing bidirectional, downward, upward respectively)
 * -> Patterns: follow AspectJ->like syntax for matching package names
 *
 * Example DSL:
 *   # comment line
 *   // another comment line
 *
 *   allow *.service.* ->^v *.repository.*
 *   forbid *.*.controller*.. ->^ ..model..
 *   ignore ..util.. ->v ..*Helper..
 */
public class DependencyRuleJudge implements DependencyJudge {

    private List<DependencyRule> rules;

    public DependencyRuleJudge(List<DependencyRule> rules) {
        this.rules = rules;
    }

    public DependencyRuleJudge() {
        this(Collections.emptyList());
    }

    /**
     * Evaluates the type of dependency between the origin and target classes in the given direction.
     *
     * @param originClass the fully qualified name of the origin class
     * @param targetClass the fully qualified name of the target class
     * @param direction the direction of the dependency (UP or DOWN)
     * @return DependencyRuleType (ALLOW, FORBID, IGNORE)
     */
    @Override
    public DependencyVerdict judge(Dependency dependency, boolean goesUp) {
        for (DependencyRule rule : rules) {
            Optional<DependencyVerdict> verdict = rule.match(dependency, goesUp);
            if (verdict.isPresent()) {
                return verdict.get();
            }
        }
        return goesUp ? DependencyVerdict.FORBIDDEN : DependencyVerdict.ALLOWED;
    }
}
