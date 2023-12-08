/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;
import java.util.List;
import java.util.Optional;

import javax.swing.JTextArea;

import org.freeplane.core.ui.components.UITools;

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
public class DependencyJudge {

    /**
     * Creates an instance of DependencyMatcher based on the provided DSL string.
     *
     * @param dependencyDSL the DSL string defining the rules
     * @return DependencyMatcher instance
     */
    public static DependencyJudge of(String dependencyDSL) {
        return new DependencyJudge(new CodeExplorerConfigurationParser(dependencyDSL).getRules());
    }

    private List<DependencyRule> rules;

    private DependencyJudge(List<DependencyRule> rules) {
        this.rules = rules;
    }

    /**
     * Evaluates the type of dependency between the origin and target classes in the given direction.
     *
     * @param originClass the fully qualified name of the origin class
     * @param targetClass the fully qualified name of the target class
     * @param direction the direction of the dependency (UP or DOWN)
     * @return DependencyRuleType (ALLOW, FORBID, IGNORE)
     */
    public DependencyVerdict judge(Dependency dependency, boolean goesUp) {
        for (DependencyRule rule : rules) {
            Optional<DependencyVerdict> verdict = rule.match(dependency, goesUp);
            if (verdict.isPresent()) {
                return verdict.get();
            }
        }
        return goesUp ? DependencyVerdict.FORBIDDEN : DependencyVerdict.ALLOWED;
    }

    public static void showHelp(String text) {
        JTextArea helpText = new JTextArea((text.trim().isEmpty() ? "" : text + "\n\n")
                 +"Rule Format:\n"
                 + "-> Rules are defined one per line in the format:\n"
                 + " [command] [originPattern] [direction] [targetPattern]\n\n"
                 + "-> Commands: allow, forbid, ignore\n"
                 + "-> Direction: ->^v, ->v, ->^ (representing bidirectional, downward, upward respectively)\n"
                 + "-> Patterns: follow AspectJ->like syntax for matching package names\n\n"
                 + "# comment line\n"
                 + "// another comment line\n\n"
                 + "Examples:\n"
                 + "\n"
                 + "  allow *.service.* ->^v *.repository.*\n"
                 + "  forbid *.*.controller*.. ->^ ..model..\n"
                 + "  ignore ..util.. ->v ..*Helper..\n"
                 + "");
        helpText.setEditable(false);
        UITools.informationMessage(helpText);
    }
}
