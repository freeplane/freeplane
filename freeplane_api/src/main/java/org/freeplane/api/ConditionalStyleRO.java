package org.freeplane.api;

public interface ConditionalStyleRO {

	/**
	 * @since 1.11.1
	 */
	boolean isActive();

	/**
	 * @since 1.11.1
	 */
	boolean isAlways();

	/**
	 * Returns true if {@link #getConditionClassSimpleName()} equals "ScriptCondition" (aka Script Filter)
	 *
	 * @since 1.11.1
	 */
	boolean hasScriptCondition();

	/**
	 * @since 1.11.1
	 */
	String getConditionClassSimpleName();

	/**
	 * @since 1.11.1
	 */
	String getScript();

	/**
	 * @since 1.11.1
	 */
	String getStyleName();

	/**
	 * In the Manage Conditional Styles dialog it's the <b>Stop</b> checkbox
	 *
	 * @since 1.11.1
	 */
	boolean isLast();
}
