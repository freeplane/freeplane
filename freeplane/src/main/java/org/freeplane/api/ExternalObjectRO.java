package org.freeplane.api;

/** External object: <code>node.externalObject</code> - read-only. */
public interface ExternalObjectRO {
	/** returns the object's uri if set or null otherwise.
	 * @since 1.2 */
	String getUri();

	/** returns the current zoom level as ratio, i.e. 1.0 is returned for 100%.
	 * If there is no external object 1.0 is returned. */
	float getZoom();

	/** @deprecated since 1.2 - use {@link #getUri()} instead. */
	@Deprecated
	String getURI();
}