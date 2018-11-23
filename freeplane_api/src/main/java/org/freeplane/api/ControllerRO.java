package org.freeplane.api;

import java.io.File;
import java.util.List;



/** Access to global state: in scripts, this is available as global variable <code>c</code> - read-only. */
public interface ControllerRO {
	/** if multiple nodes are selected returns one (arbitrarily chosen)
	 * selected node or the selected node for a single node selection. */
	Node getSelected();

	/** A read-only list of selected nodes. That is you cannot select a node by adding it to the returned list. */
	List<? extends Node> getSelecteds();

	/** returns {@code List<? extends Node>} sorted by the node's vertical position.
	 *
	 * @param differentSubtrees if true
	 *   children/grandchildren/grandgrandchildren/... nodes of selected
	 *   parent nodes are excluded from the result. */
	List<? extends Node> getSortedSelection(boolean differentSubtrees);

	/**
	 * returns Freeplane version.
	 * Use it like this:
	 * <pre>{@code
	 *   
	 *   
	 *
	 *   def required = FreeplaneVersion.getVersion("1.1.2");
	 *   if (c.freeplaneVersion < required)
	 *       UITools.errorMessage("Freeplane version " + c.freeplaneVersion
	 *           + " not supported - update to at least " + required);
	 * }</pre>
	 */
	FreeplaneVersion getFreeplaneVersion();

	/** returns the directory where user settings, logfiles, templates etc. are stored.
	 * @since 1.2 */
	File getUserDirectory();


	/**
	 * Starting from the root node, recursively searches for nodes (in breadth-first sequence) for which
	 * <code>closure.call(node)</code> returns true.
	 * <p>
	 * A find method that uses a lambda ("block") for simple custom searches. As this closure
	 * will be called with a node as an argument (to be referenced by <code>it</code>) the search can
	 * evaluate every node property, like attributes, icons, node text or notes.
	 * <p>
	 * Examples:
	 * <pre>
	 *    def nodesWithNotes = c.find{ it.noteText != null }
	 *
	 *    def matchingNodes = c.find{ it.text.matches(".*\\d.*") }
	 *    def texts = matchingNodes.collect{ it.text }
	 *    print "node texts containing numbers:\n " + texts.join("\n ")
	 * </pre>
	 * See {@link Node#find(NodeCondition)} for searches on subtrees.
	 * @param condition a lambda that returns a boolean value. The closure will receive
	 *        a NodeModel as an argument which can be tested for a match.
	 * @return all nodes for which <code>closure.call(NodeModel)</code> returns true.
	 */
	List<? extends Node> find(NodeCondition condition);

	/**
	 * Returns all nodes of the map in breadth-first order, that is, for the following map,
	 * <pre>
	 *  1
	 *    1.1
	 *      1.1.1
	 *      1.1.2
	 *    1.2
	 *  2
	 * </pre>
	 * [1, 1.1, 1.1.1, 1.1.2, 1.2, 2] is returned.
	 * See {@link Node#find(NodeCondition)} for searches on subtrees.
	 * @see #findAllDepthFirst()
	 * @since 1.2 */
	List<? extends Node> findAll();

	/**
	 * Returns all nodes of the map in depth-first order, that is, for the following map,
	 * <pre>
	 *  1
	 *    1.1
	 *      1.1.1
	 *      1.1.2
	 *    1.2
	 *  2
	 * </pre>
	 * [1.1.1, 1.1.2, 1.1, 1.2, 1, 2] is returned.
	 * See {@link Node#findAllDepthFirst()} for subtrees.
	 * @since 1.2 */
	List<? extends Node> findAllDepthFirst();

	/** returns the current zoom factor. A value of 1 means 100%.
	 * @since 1.2 */
	float getZoom();

	/** returns false if the system 'nonInteractive' is set. This can be used in actions to not open dialogs etc.
	 * @since 1.2 */
	boolean isInteractive();

	/** returns a list of export type descriptions that can be used to specify a specific export type
	 * in {@link #export(Map, File, String, boolean)}. These descriptions are internationalized.
	 * @since 1.3.5 */
	List<String> getExportTypeDescriptions();

    /** exports map to destination file, example:
     * <pre>
     *   println c.exportTypeDescriptions.join('\n')
     *   boolean overwriteExistingFile = true
     *   c.export(node.map, new File('/tmp/t.png'), 'Portable Network Graphic (PNG) (.png)', overwriteExistingFile)
     * </pre>
     * @param exportTypeDescription Use {@link #getExportTypeDescriptions()} to look up available exportTypes.
     *   Note that the file format does not suffice to specify a specific export since there may be more than
     *   one, as for HTML.
     * @since 1.3.5 */
    void export(Map map, File destinationFile, String exportTypeDescription, boolean overwriteExisting);
}