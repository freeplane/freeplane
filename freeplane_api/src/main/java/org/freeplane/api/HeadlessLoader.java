package org.freeplane.api;

import java.io.File;

/**
 * API for accessing or loading map from given location.
 *
 * @since 1.7.1
 */
public interface HeadlessLoader {
	/**
	 * Sets map location to given file after loading.
	 *
	 * @since 1.7.1
	 */
	Loader newMapLocation(File file);

	/**
	 * Sets map location to given file after loading.
	 *
	 * @since 1.7.1
	 */
	Loader newMapLocation(String file);

	/**
	 * Removes associated location after loading.
	 *
	 * @since 1.7.1
	 */
	Loader unsetMapLocation();

	/**
	 * Saves map after loading under new associated location
	 *
	 * @since 1.7.1
	 */
	Loader saveAfterLoading();

	/**
	 * Creates and returns a map or selects and returns a previously loaded map.
	 *
	 * @since 1.7.5
	 */
	Map load();

	/**
	 * Creates and returns a map or selects and returns a previously loaded map.
	 *
	 * Same as {@link #load()}
	 *
	 * @since 1.7.1
	 */
	Map getMap();
}
