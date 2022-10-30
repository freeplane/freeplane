package org.freeplane.api;

public interface ConditionalStyleRO {

	/**
	 * @since 1.10.5
	 */
	boolean isActive();

	/**
	 * @since 1.10.5
	 */
	boolean isAlways();

	/**
	 * Returns true if {@link #getConditionClassSimpleName()} equals "ScriptCondition" (aka Script Filter)
	 *
	 * @since 1.10.5
	 */
	boolean isScriptCondition();

	/**
	 * @since 1.10.5
	 */
	String getConditionClassSimpleName();

	/**
	 * @since 1.10.5
	 */
	String getScript();

	/**
	 * @since 1.10.5
	 */
	String getStyleName();

	/**
	 * In the Manage Conditional Styles dialog it's the <b>Stop</b> checkbox
	 *
	 * @since 1.10.5
	 */
	boolean isLast();
}
