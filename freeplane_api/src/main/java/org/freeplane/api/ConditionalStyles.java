package org.freeplane.api;

public interface ConditionalStyles {

	/**
	 * @throws IllegalArgumentException if styleName is not found
	 * @since 1.10.5
	 */
	void add(boolean isActive, String script, String styleName, boolean isLast);

	/**
	 * Adds a copy of conditionalStyle.
	 * It can be used to copy a conditional style between nodes or between maps.
	 *
	 * @since 1.10.5
	 */
	void add(ConditionalStyleRO conditionalStyle);

	/**
	 * @throws IllegalArgumentException if styleName is not found
	 * @since 1.10.5
	 */
	void insert(int position, boolean isActive, String script, String styleName, boolean isLast);

	/**
	 * Inserts a copy of conditionalStyle in position.
	 * It can be used to copy a conditional style between nodes or between maps.
	 *
	 * @since 1.10.5
	 */
	void insert(int position, ConditionalStyleRO conditionalStyle);

	/**
	 * @since 1.10.5
	 */
	void move(int position, int toPosition);

	/**
	 * It can be used to move a conditional style between nodes or between maps.
	 * <pre>
	 * def cStyle = node.conditionalStyles.remove(0)
	 * node.parent.conditionalStyles.add(cStyle)
	 * </pre>
	 *
	 * @return the removed ConditionalStyle
	 * @since 1.10.5
	 */
	ConditionalStyle remove(int position);
}
