package org.freeplane.api;

/**
 * Node's or map's conditional style
 * <p>
 * In the Manage Conditional Styles dialog, it's a row in the table.
 * </p>
 * <p>
 * See also {@link ConditionalStyles}
 * </p>
 */
public interface ConditionalStyle extends ConditionalStyleRO {

	/**
	 * @since 1.11.1
	 */
	void setActive(boolean isActive);

	/**
	 * Sets a Groovy script as the Script Filter for this Conditional Style.
	 * <b>script</b> set to <code>null</code> means "always".
	 * If {@link ConditionalStyleRO#getConditionClassSimpleName()} is different from "ScriptCondition",
	 * setting <b>script</b> will change the condition type to <b>ScriptCondition</b> (aka Script Filter).
	 *
	 * @since 1.11.1
	 */
	void setScript(String script);

	/**
	 * @throws IllegalArgumentException when styleName is not found
	 * @since 1.11.1
	 */
	void setStyleName(String styleName);

	/**
	 * In the Manage Conditional Styles dialog it's the <b>Stop</b> checkbox
	 *
	 * @since 1.11.1
	 */
	void setLast(boolean isLast);

	/**
	 * Moves the conditional style to the specified position
	 *
	 * @throws IndexOutOfBoundsException if the position is out of range (position < 0 || position >= ConditionalStyles.size())
	 * @since 1.11.1
	 */
	void moveTo(int position);

	/**
	 * Removes the conditional style from the table
	 *
	 * @return the removed ConditionalStyle
	 * @since 1.11.1
	 */
	ConditionalStyle remove();
}
