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
	 * @since 1.7.5
	 */
	HeadlessLoader loader(File file);

	/**
	 * Load mind map from URL.
	 * @since 1.7.5
	 */
	HeadlessLoader loader(URL url);

	/**
	 * Load mind map from string input.
	 * @since 1.7.5
	 */
	HeadlessLoader loader(String input);

	/** @deprecated since 1.7.5 - use {@link #loader(File)} */
	@Deprecated
	HeadlessLoader load(File file);

	/** @deprecated since 1.7.5 - use {@link #loader(URL)} */
	@Deprecated
	HeadlessLoader load(URL url);

	/** @deprecated since 1.7.5 - use {@link #loader(String)} */
	@Deprecated
	HeadlessLoader load(String input);

	/**
	 * Create executable script from file.
	 * @since 1.7.1
	 */
	Script script(File source);

	/**
	 * Create executable script from given argument.
	 *
	 * The script is executed with all permissions.
	 *
	 * @since 1.7.2
	 */
	Script script(String script, String type);

}