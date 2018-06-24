package org.freeplane.api;

import java.io.File;

/** External object: <code>node.externalObject</code> - read-write. */
public interface ExternalObject extends ExternalObjectRO {
    /** setting null uri means remove external object.
     * Starting with Freeplane 1.2.23 there is an additional setUri(Object) method that also accepts File,
     * URI and URL arguments.
     * @since 1.2 */
	void setUri(String target);

	/** setting null uri means remove external object. */
	void setFile(File target);

	/** set to 1.0 to set it to 100%. If the node has no object assigned this method does nothing. */
	void setZoom(float zoom);

	/** @deprecated since 1.2 - use {@link #setUri(String)} instead. */
	@Deprecated
	void setURI(String uri);
}