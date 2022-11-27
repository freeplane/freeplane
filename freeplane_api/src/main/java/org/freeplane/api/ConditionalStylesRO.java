package org.freeplane.api;

/**
 * Node's or map's conditional-styles table:
 * <code>node.conditionalStyles</code> or <code>node.mindMap.conditionalStyles</code> - read-only.
 * <p>
 * In the Manage Conditional Styles dialog it's the entire table. Each row in the table is a separate {@link ConditionalStyle}.
 * </p>
 * ConditionalStyles can be iterated over, e.g.
 * <pre>
 *     // get a list of conditional styles (ConditionalStyle items)
 *     node.conditionalStyles.collect()
 *
 *     // get a list of conditional styles, each as a string (description)
 *     node.conditionalStyles.collect { it.toString() }
 *
 *     // get the number of conditional styles in the table
 *     node.conditionalStyles.size()
 *
 *     // get the first conditional style in the table
 *     node.conditionalStyles[0]
 *
 *     // find all conditional styles with the style styles.important (aka Important) and deactivate them
 *     node.conditionalStyles.findAll { it.styleName == 'styles.important' }.each { it.active = false }
 *
 *     // find the first conditional style with ScriptCondition (aka Script Filter) and remove it
 *     node.conditionalStyles.find { it.hasScriptCondition() }?.remove()
 *
 *     // remove all, i.e. each ConditionalStyle item
 *     node.conditionalStyles.collect().each { it.remove() }
 * </pre>
 * See also {@link ConditionalStyles}
 */
public interface ConditionalStylesRO extends Iterable<ConditionalStyle> {/**/}
