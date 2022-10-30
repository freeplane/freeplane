package org.freeplane.api;

public interface ConditionalStyle {

	/**
	 * @since 1.10.5
	 */
	void setActive(boolean isActive);

	/**
	 * Sets a Groovy script as the Script Filter for this Conditional Style.
	 * <b>script</b> set to null means "always".
	 * If {@link ConditionalStyleRO#getConditionClassSimpleName()} is different from "ScriptCondition",
	 * setting <b>script</b> will change the condition type to <b>ScriptCondition</b> (aka Script Filter).
	 *
	 * @since 1.10.5
	 */
	void setScript(String script);

	/**
	 * @throws IllegalArgumentException when styleName is not found
	 * @since 1.10.5
	 */
	void setStyleName(String styleName);

	/**
	 * In the Manage Conditional Styles dialog it's the <b>Stop</b> checkbox
	 *
	 * @since 1.10.5
	 */
	void setLast(boolean isLast);

	/**
	 * @since 1.10.5
	 */
	void moveTo(int position);

	/**
	 * @return the removed ConditionalStyle
	 * @since 1.10.5
	 */
	ConditionalStyle remove();
}
