package org.freeplane.api;

import java.net.URL;
import java.util.Iterator;
import java.util.List;

/** Node's icons: <code>node.icons</code> - read-only. */
public interface IconsRO {
	/** returns the name of the icon at the given index (starting at 0) or null if {@code index >= size}.
	 * Use it like this: <pre>
	 *   def secondIconName = node.icons[1]
	 * </pre>
	 * @since 1.2 */
	String getAt(int index);

	/** returns the name of the first icon if the node has an icon assigned or null otherwise. Equivalent: <code>node.icons[0]</code>.
	 * @since 1.2 */
	String getFirst();

	/** returns true if the node has an icon of this name.
	 * @since 1.2 */
	boolean contains(String name);

	/** returns the number of icons the node has.
	 * @since 1.2 */
	int size();

	/** returns a read-only list of the names of the icons the node has. Think twice before you use this method
	 * since it leads to ugly code, e.g. use <code>node.icons.first</code> or <code>node.icons[0]</code> instead of
	 * <code>node.icons.icons[0]</code>. Perhaps you could also use iteration over icons, see. */
	List<String> getIcons();

	/** returns a list of the urls of the icons the node has. */
	List<URL> getUrls();

    /** allows application of Groovy collection methods like each(), collect(), ...
     * <pre>
     *   def freeIcons = node.icons.findAll { it.startsWith('free') }
     * </pre>
     * @since 1.3.2 */
    Iterator<String> iterator();
}