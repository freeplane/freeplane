package org.freeplane.api;

import java.io.File;
import java.net.URI;

/** Node's link: <code>node.link</code> - read-only.
 * <p>
 * None of the getters will throw an exception, even if you call, e.g. getNode() on a File link.
 * Instead they will return null. To check the link type evaluate getUri().getScheme() or the result
 * of the special getters.*/
public interface LinkRO {
	/** returns the link text, a stringified URI, if a link is defined and null otherwise.
	 * @since 1.2 */
	String getText();

	/** returns the link as URI if defined and null otherwise. Won't throw an exception.
	 * @since 1.2 */
	URI getUri();

	/** returns the link as File if defined and if the link target is a valid File URI and null otherwise.
	 * @see File#File(URI)
	 * @since 1.2 */
	File getFile();

	/** returns the link as Node if defined and if the link target is a valid local link to a node and null otherwise.
	 * @since 1.2 */
	Node getNode();

	/** @deprecated since 1.2 - use {@link #getText()} instead. */
	@Deprecated
	String get();
}