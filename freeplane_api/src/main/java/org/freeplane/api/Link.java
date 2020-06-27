package org.freeplane.api;

import java.io.File;
import java.net.URI;

/** Node's link: <code>node.link</code> - read-write.
 * To set links use the attributes of the {@link Link} and {@link LinkRO} object:
 * <pre>
 * // a normal href
 * node.link.text = 'http://www.google.com'
 * // create a node to the parent node
 * node.link.node = node.parent
 * // if you have a URI object
 * node.link.uri = new URI('http://www.google.com')
 * // file
 * node.link.file = map.file
 * </pre>
 */
public interface Link extends LinkRO {
	/** target is a stringified URI. Removes any link if uri is null.
	 * To get a local link (i.e. to another node) target should be: "#" + nodeId or better use setNode(Node).
	 * @throws IllegalArgumentException if target is not convertible into a {@link URI}.
	 * @since 1.2 */
	void setText(String target);

	/** sets target to uri. Removes any link if uri is null.
	 * @since 1.2 */
	void setUri(URI uri);

	/** sets target to file. Removes any link if file is null.
	 * @since 1.2 */
	void setFile(File file);

	/** target is a node of the same map. Shortcut for setTarget("#" + node.nodeId)
	 * Removes any link if node is null.
	 * @throws IllegalArgumentException if node belongs to another map.
	 * @since 1.2 */
	void setNode(Node node);

	/** @deprecated since 1.2 - use {@link #setText(String)} instead.
	 * @return true if target could be converted to an URI and false otherwise. */
	@Deprecated
	boolean set(String target);

	/** removes the link. Same as <code>node.link.text = null</code>.
	 * @return <code>true</code> if there was a link to remove.
	 * @since 1.4 */
	boolean remove();
}
