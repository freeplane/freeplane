package org.freeplane.api;

import java.awt.Color;


/** The map a node belongs to: <code>node.map</code> - read-write. */
public interface Map extends MapRO {
	/**
	 * closes a map. Note that there is <em>no undo</em> for this method!
	 * @param force close map even if there are unsaved changes.
	 * @param allowInteraction {@code if (allowInteraction && ! force)} a saveAs dialog will be opened if there are
	 *        unsaved changes.
	 * @return false if the saveAs was cancelled by the user and true otherwise.
	 * @throws RuntimeException if the map contains changes and parameter force is false.
	 * @since 1.2
	 */
	boolean close(boolean force, boolean allowInteraction);

	/**
	 * saves the map to disk. Note that there is <em>no undo</em> for this method.
	 * @param allowInteraction if a saveAs dialog should be opened if the map has no assigned URL so far.
	 * @return false if the saveAs was cancelled by the user and true otherwise.
	 * @throws RuntimeException if the map has no assigned URL and parameter allowInteraction is false.
	 * @since 1.2
	 */
	boolean save(boolean allowInteraction);

	/** @since 1.2 */
	void setSaved(boolean isSaved);

	/** Sets the map (frame/tab) title. Note that there is <em>no undo</em> for this method!
	 * @since 1.2 */
	void setName(String title);

	/** @since 1.2 */
	void setBackgroundColor(Color color);

	/** @param rgbString a HTML color spec like #ff0000 (red) or #222222 (darkgray).
	 *  @since 1.2 */
	void setBackgroundColorCode(String rgbString);

	/** install a lambda as the current filter in this map. If <code>closure</code> is null then filtering will
	 * be disabled. The filter state of a node can be checked by {@link Node#isVisible()}. <br>
	 * To undo filtering use <em>Tools &rarr; Undo</em>. After execution of the following you have to use it seven times to
	 * return to the initial filter state.
	 * <pre>
	 * // show only matching nodes
	 * node.map.filter{ it.text.contains("todo") }
	 * // equivalent:
	 * node.map.filter = { it.text.contains("todo") }
	 *
	 * // show ancestors of matching nodes
	 * node.map.filter(true, false){ it.text.contains("todo") }
	 * // equivalent:
	 * node.map.setFilter(true, false, { it.text.contains("todo") })
	 *
	 * // show descendants of matching nodes
	 * node.map.filter(false, true){ it.text.contains("todo") }
	 * // equivalent:
	 * node.map.setFilter(false, true, { it.text.contains("todo") })
	 *
	 * // remove filter
	 * node.map.filter = null
	 * </pre>
	 * @since 1.2 */
	public void filter(final NodeCondition condition);

	/** alias for {@link #filter(Closure)}. Enables assignment to the <code>filter</code> property.
	 * @since 1.2 */
	public void setFilter(final NodeCondition condition);

	/** With {@link #filter(Closure)} neither ancestors not descendants of the visible nodes are shown. Use this
	 * method to control these options.
	 * @see #filter(Closure)
	 * @since 1.2 */
	public void filter(final boolean showAncestors, final boolean showDescendants, final NodeCondition condition);

	/** alias for {@link #setFilter(boolean, boolean, Closure)}
	 * @see #filter(Closure)
	 * @since 1.2 */
	public void setFilter(final boolean showAncestors, final boolean showDescendants, final NodeCondition condition);

	/** reinstalls the previously undone filter if there is any.
	 * Note: undo/redo for filters is separate to the undo/redo for other map state.
	 *  @since 1.2 */
	public void redoFilter();

	/** removes the current filter and reinstalls the previous filter if there is any.
	 * Note: undo/redo for filters is separate to the undo/redo for other map state.
	 *  @since 1.2 */
	public void undoFilter();

	/** returns an accessor to the map specific storage. The value is never null
	 *  @since 1.3.6 */
	public Properties getStorage();
}
