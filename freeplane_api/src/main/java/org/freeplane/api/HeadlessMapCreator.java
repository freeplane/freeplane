package org.freeplane.api;

import java.io.File;
import java.net.URL;

/**
 * API for creating and accessing mind map without UI.
 * @since 1.7.1
 */
public interface HeadlessMapCreator {
	/**
	 * Load mind map from file.
	 * @since 1.7.1
	 */
	HeadlessLoader load(File file);

	/**
	 * Load mind map from URL.
	 * @since 1.7.1
	 */
	HeadlessLoader load(URL url);

	/**
	 * Load mind map from string input.
	 * @since 1.7.1
	 */
	HeadlessLoader load(String input);

	/**
	 * Create executable script from file.
	 * @since 1.7.1
	 */
	Script script(File source);

	/**
	 * Create executable script from file.
	 * @since 1.7.2
	 */
	Script script(String script, String type);

}