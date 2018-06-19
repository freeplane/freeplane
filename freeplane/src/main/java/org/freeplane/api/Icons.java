package org.freeplane.api;

import java.util.Collection;



/** Node's icons: <code>node.icons</code> - read-write. */
public interface Icons extends IconsRO {
	/**
	 * adds an icon to a node if an icon for the given key can be found. The same icon can be added multiple
	 * times.
	 * <pre>
	 *   println("all available icon keys: " + FreeplaneIconUtils.listStandardIconKeys())
	 *   node.icons.addIcon("button_ok")
	 * </pre>
	 * @see FreeplaneIconFactory */
	void add(String name);

	/** @since 1.4 */
	void addAll(Collection<String> names);

	/** @since 1.4 */
	void addAll(IconsRO icons);

	/** @deprecated since 1.2 - use {@link #add(String)} instead. */
	@Deprecated
	void addIcon(String name);

	/** deletes the icon at the given index, returns true if success (icon existed). */
	boolean remove(int index);

	/** deletes first occurence of icon with the given name, returns true if success (icon existed). */
	boolean remove(String name);

	/** @deprecated since 1.2 - use {@link #remove(String)} instead. */
	@Deprecated
	boolean removeIcon(String name);

	/** removes all icons.
	 * @since 1.2 */
	void clear();
}