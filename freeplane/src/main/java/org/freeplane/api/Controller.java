package org.freeplane.api;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import javax.swing.Icon;



/** Access to global state: in scripts, this is available as global variable <code>c</code> - read-write. */
public interface Controller extends ControllerRO, HeadlessMapCreator {
	void centerOnNode(Node center);

	/** Starts editing node, normally in the inline editor. Does not block until edit has finished.
	 * @since 1.2.2 */
	void edit(Node node);

	/** opens the appropriate popup text editor. Does not block until edit has finished.
	 * @since 1.2.2 */
	void editInPopup(Node node);

	void select(Node toSelect);

	/** selects multiple Nodes.
	 * @since 1.4 */
	void select(Collection<? extends Node> toSelect);

	/** selects branchRoot and all children */
	void selectBranch(Node branchRoot);

	/** same as {@link #select(Collection)} */
	void selectMultipleNodes(Collection<? extends Node> toSelect);

	/** reset undo / redo lists and deactivate Undo for current script */
	void deactivateUndo();

	/** invokes undo once - for testing purposes mainly.
	 * @since 1.2 */
	void undo();

	/** invokes redo once - for testing purposes mainly.
	 * @since 1.2 */
	void redo();

	/** The main info for the status line with key="standard", use null to remove. Removes icon if there is one. */
	void setStatusInfo(String info);

	/** Info for status line, null to remove. Removes icon if there is one.
	 * @see #setStatusInfo(String, String, String) */
	void setStatusInfo(String infoPanelKey, String info);

	/** Info for status line - text and icon - null stands for "remove" (text or icon)
	 * @param infoPanelKey "standard" is the left most standard info panel. If a panel with
	 *        this name doesn't exist it will be created.
	 * @param info Info text
	 * @param iconKey key as those that are used for nodes (see {@link Icons#addIcon(String)}).
	 * <pre>
	 *   println("all available icon keys: " + FreeplaneIconUtils.listStandardIconKeys())
	 *   c.setStatusInfo("standard", "hi there!", "button_ok");
	 * </pre>
	 * @see FreeplaneIconFactory
	 * @since 1.2 */
	void setStatusInfo(String infoPanelKey, String info, String iconKey);

	/** @deprecated since 1.2 - use {@link #setStatusInfo(String, String, String)} */
	@Deprecated
	void setStatusInfo(String infoPanelKey, Icon icon);

	/** opens a new map with a default name in the foreground.
	 * @since 1.2 */
	Map newMap();

	/** opens a new map for url in the foreground if it isn't opened already.
	 * @since 1.2 */
	Map newMap(URL url);

	/** opens a new map based on given template.
	 * @since 1.5 */
	public Map newMapFromTemplate(File templateFile);

	/** a value of 1 means 100%.
	 * @since 1.2 */
	void setZoom(final float ratio);

	/** a list of all opened maps.
	 * @since 1.5 */
	List<? extends Map> getOpenMaps();

}