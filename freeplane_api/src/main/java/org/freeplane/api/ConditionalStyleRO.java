package org.freeplane.api;

public interface ConditionalStyleRO {

	/**
	 * @since 1.10.6~TBC
	 */
	boolean isActive();

	/**
	 * @since 1.10.6~TBC
	 */
	boolean isAlways();

	/**
	 * Returns true if {@link #getConditionClassSimpleName()} equals "ScriptCondition" (aka Script Filter)
	 *
	 * @since 1.10.6~TBC
	 */
	boolean hasScriptCondition();

	/**
	 * @since 1.10.6~TBC
	 */
	String getConditionClassSimpleName();

	/**
	 * @since 1.10.6~TBC
	 */
	String getScript();

	/**
	 * @since 1.10.6~TBC
	 */
	String getStyleName();

	/**
	 * In the Manage Conditional Styles dialog it's the <b>Stop</b> checkbox
	 *
	 * @since 1.10.6~TBC
	 */
	boolean isLast();
}
