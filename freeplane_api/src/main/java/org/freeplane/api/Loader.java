package org.freeplane.api;

/**
 * API for selecting or creating map views.
 *
 * @since 1.7.1
 */
public interface Loader extends HeadlessLoader {
	/**
	 * Creates and selects a map view or selects already existing map view.
	 *
	 * @since 1.7.1
	 */
	Loader withView();
	/**
	 * Selects given node after loading.
	 *
	 * The map view is created if needed.
	 *
	 * @since 1.7.1
	 */
	Loader selectNodeById(String nodeId);
}
