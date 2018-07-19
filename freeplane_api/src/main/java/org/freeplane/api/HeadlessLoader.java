package org.freeplane.api;

import java.io.File;

/**
 * API for accessing or loading map from given location.
 *
 * @since 1.6.16
 */
public interface HeadlessLoader {
	/**
	 * Sets map location to given file after loading.
	 *
	 * @since 1.6.16
	 */
	Loader newMapLocation(File file);

	/**
	 * Sets map location to given file after loading.
	 *
	 * @since 1.6.16
	 */
	Loader newMapLocation(String file);

	/**
	 * Removes associated location after loading.
	 *
	 * @since 1.6.16
	 */
	Loader unsetMapLocation();

	/**
	 * Saves map after loading under new associated location
	 *
	 * @since 1.6.16
	 */
	Loader saveAfterLoading();

	/**
	 * Creates and returns a map or selects and returns a previously loaded map.
	 *
	 * @since 1.6.16
	 */
	Map getMap();

}
