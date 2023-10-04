package org.freeplane.api;

/**
 * Node's or map's conditional-styles table:
 * <code>node.conditionalStyles</code> or <code>node.mindMap.conditionalStyles</code> - read-only.
 * <p>
 * In the Manage Conditional Styles dialog, it's the entire table. Each row in the table is a separate {@link ConditionalStyle}/{@link ConditionalStyleRO}.
 * </p>
 * <p>
 * Actions known from the Manage Conditional Styles dialog can be called on ConditionalStyles
 * </p>
 * <pre>
 *     // add a conditional style to the end of the table
 *     node.conditionalStyles.add(
 *       true, // isActive
 *       "node.text == 'Sunday'", // script
 *       'styles.important', // styleName
 *       false) // isLast (aka stop)
 *
 *     // insert a conditional style at the beginning of the table, with the condition "always"
 *     node.conditionalStyles.insert(0, true, null, 'defaultstyle.floating', false)
 *
 *     // move the third conditional style to the top of the table
 *     node.conditionalStyles.move(2, 0)
 *
 *     // remove the first conditional style from the table
 *     node.conditionalStyles.remove(0)
 *
 *     // remove all, i.e. each ConditionalStyle item
 *     node.conditionalStyles.each { it.remove() }
 * </pre>
 * <p>
 * ConditionalStyles can be iterated over, e.g.
 * </p>
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
 * </pre>
 */
public interface ConditionalStyles extends Iterable<ConditionalStyle> {

	/**
	 * Adds a conditional style to the end of the table
	 *
	 * @throws IllegalArgumentException if styleName is not found
	 * @since 1.11.1
	 */
	void add(boolean isActive, String script, String styleName, boolean isLast);

	/**
	 * Adds a copy of conditionalStyle to the end of the table.
	 * It can be used to copy a conditional style between nodes or between maps.
	 *
	 * @since 1.11.1
	 */
	void add(ConditionalStyleRO conditionalStyle);

	/**
	 * Inserts a conditional style at the specified position.
	 *
	 * @throws IllegalArgumentException if styleName is not found
	 * @since 1.11.1
	 */
	void insert(int position, boolean isActive, String script, String styleName, boolean isLast);

	/**
	 * Inserts a copy of conditionalStyle at the specified position.
	 * It can be used to copy a conditional style between nodes or between maps.
	 *
	 * @since 1.11.1
	 */
	void insert(int position, ConditionalStyleRO conditionalStyle);

	/**
	 * Moves the conditional style found at <b>position</b> to <b>toPosition</b>
	 *
	 * @since 1.11.1
	 */
	void move(int position, int toPosition);

	/**
	 * It can be used to move a conditional style between nodes or between maps.
	 * <pre>
	 *     def cStyle = node.conditionalStyles.remove(0)
	 *     node.parent.conditionalStyles.add(cStyle)
	 * </pre>
	 *
	 * @return the removed ConditionalStyle
	 * @since 1.11.1
	 */
	ConditionalStyle remove(int position);
}
