package org.freeplane.api;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/** The map a node belongs to: <code>node.map</code> - read-write. */
public interface MindMap extends MindMapRO {
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

	/**
	 * saves the map to disk. Note that there is <em>no undo</em> for this method.
	 * @param file the location of the file to be saved.
	 * @return false if the saveAs was cancelled by the user and true otherwise.
	 * @throws RuntimeException if the map has no assigned URL and parameter allowInteraction is false.
	 * @since 1.2
	 */
	boolean saveAs(File file);

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

	/** With {@link #filter(boolean, boolean, NodeCondition)} neither ancestors not descendants of the visible nodes are shown.
	 * @see #filter(boolean, boolean, NodeCondition)
	 * @since 1.2 */
	public void filter(final NodeCondition condition);

	/** alias for {@link #filter(NodeCondition)}. Enables assignment to the <code>filter</code> property.
	 * @since 1.2 */
	public void setFilter(final NodeCondition condition);

	/** install a lambda as the current filter in this map. If <code>condition</code> is null then filtering will
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
	 * @param showAncestors whether to show ancestors or not.
	 * @param showDescendants whether to show descendant or not.
	 * @param condition filter the map using this lamda.
	 * @since 1.2 */
	public void filter(final boolean showAncestors, final boolean showDescendants, final NodeCondition condition);
	
	/**
	 * Hides nodes matching given condition.
	 * 
	 * @see #filter(boolean, boolean, NodeCondition)
	 * 
	 * @param hideAncestors whether to hide ancestors or not.
	 * @param hideDescendants whether to hide descendant or not.
	 * @param condition filter the map using this lamda.
	 * @since 1.8.1 
	 */
    public void hide(final boolean hideAncestors, final boolean hideDescendants, final NodeCondition condition);
	/** alias for {@link #filter(boolean, boolean, NodeCondition)}
	 * @see #filter(boolean, boolean, NodeCondition)
	 * @deprecated use filter
	 * @since 1.2 */
	@Deprecated
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

	/**
	 * Evaluate all formulas in the map.
	 *
	 * Each formula in the map is evaluated not depending on if it was already cached.
	 *  @since 1.7.2 */
	public void evaluateAllFormulas();

	/**
	 * Evaluate probably changed formulas in the map.
	 *
	 * Each formula not having valid result in the cache is evaluated.
	 *  @since 1.7.2 */
	void evaluateOutdatedFormulas();
	
	void addListener(NodeChangeListener listener);
	
	void removeListener(NodeChangeListener listener);

	List<NodeChangeListener> getListeners();
	
    /**
     * 
     * Copies a style from another mind map into this mind map.
     *
     * @since 1.9.8
     */
	void copyStyleFrom(MindMap source, String styleName);

    /**
     *
     * Copies a style and its conditional style rules from another mind map into this mind map.
     * 
     * @since 1.9.8
     */
    void copyConditionalStylesFrom(MindMap source, String styleName);

    /**
     *
     * Copies one or more user styles from another mind map into this one.
     *
     * @param source mind map containing the desired style(s)
     * @param includeConditionalRules whether to include conditional style rules or not.
     * @param styleNameFilters one or more strings to indicate which styles should be copied (REGEX match)
     * @return list with the names of the copied styles
     * @since 1.11.9
     */
    List<String> copyUserStylesFrom(MindMap source, boolean includeConditionalRules, String... styleNameFilters);

    /**
     *
     * Copies one or more user styles from another mind map into this one.
     *
     * @param source mind map containing the desired style(s)
     * @param includeConditionalRules whether to include conditional style rules or not.
     * @param styleNameFilters ArrayList of strings to indicate which styles should be copied (REGEX match)
     * @return list with the names of the copied styles
     * @see #copyUserStylesFrom(MindMap, boolean, String...)
     * @since 1.11.9
     */
    List<String> copyUserStylesFrom(MindMap source, boolean includeConditionalRules, ArrayList<String> styleNameFilters);

    /**
     *
     * Copies all user styles and their conditional style rules from another mind map into this one.
     * <p>
     * <b>Note:</b> equivalent to: {@code     copyUserStylesFrom(source, true, ".*")}
     *
     * @param source mind map containing the desired style(s)
     * @return list with the names of the copied styles
     * @see #copyUserStylesFrom(MindMap, boolean, String...)
     * @since 1.11.9
     */
    List<String> copyUserStylesFrom(MindMap source);

    /**
     *
     * Copies one or more user styles and their conditional style rules from another mind map into this one.
     * <p>
     * <b>Note:</b> equivalent to: {@code     copyUserStylesFrom(source, true, styleNameFilters)}
     *
     * @param source mind map containing the desired style(s)
     * @param styleNameFilters one or more strings to indicate which styles should be copied (REGEX match)
     * @return list with the names of the copied styles
     * @see #copyUserStylesFrom(MindMap, boolean, String...)
     * @since 1.11.9
     */
    List<String> copyUserStylesFrom(MindMap source, String... styleNameFilters);

    /**
     *
     * Copies one or more user styles and their conditional style rules from another mind map into this one.
     * <p>
     * <b>Note:</b> equivalent to: {@code     copyUserStylesFrom(source, true, styleNameFilters)}
     *
     * @param source mind map containing the desired style(s)
     * @param styleNameFilters ArrayList of strings to indicate which styles should be copied (REGEX match)
     * @return list with the names of the copied styles
     * @see #copyUserStylesFrom(MindMap, boolean, String...)
     * @since 1.11.9
     */
    List<String> copyUserStylesFrom(MindMap source, ArrayList<String> styleNameFilters);

    /**
     *
     * Copies all user styles from another mind map into this one.
     * <p>
     * <b>Note:</b> equivalent to: {@code    copyUserStylesFrom(source, includeConditionalRules, ".*")}
     *
     *
     * @param source mind map containing the desired style(s)
     * @param includeConditionalRules whether to include conditional style rules or not.
     * @return list with the names of the copied styles
     * @see #copyUserStylesFrom(MindMap, boolean, String...)
     * @since 1.11.9
     */
    List<String> copyUserStylesFrom(MindMap source, boolean includeConditionalRules);
}
