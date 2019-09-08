package org.freeplane.api;

import java.awt.Color;
import java.io.File;

/** The map a node belongs to: <code>node.map</code> - read-only. 
 * 
 * @since 1.7.10
 * */
public interface MindMapRO {
	/** @since 1.2 */
	Node getRoot();

	/** @deprecated since 1.2 - use {@link #getRoot()} instead. */
	@Deprecated
	Node getRootNode();

	/** get node by id.
	 * @return the node if the map contains it or null otherwise. */
	Node node(String id);

	/** returns the filenname of the map as a java.io.File object if available or null otherwise. */
	File getFile();

	/** returns the title of the MapView.
	 * @since 1.2 */
	String getName();

	/** @since 1.2 */
	boolean isSaved();

    /** @since 1.2 */
    Color getBackgroundColor();

    /** returns HTML color spec like #ff0000 (red) or #222222 (darkgray).
     *  @since 1.2 */
    String getBackgroundColorCode();
}