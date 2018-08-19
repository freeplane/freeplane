package org.freeplane.api;

import java.io.PrintStream;

/**
 * API for executing scripts.
 *
 * @since 1.7.1
 */
public interface Script {
	/**
	 * With permission to start applications.
	 * @since 1.7.1
	 */
	Script startingApplications();

	/**
	 * With permission to open network connections.
	 * @since 1.7.1
	 */
	Script accessingNetwork();

	/**
	 * With permission to read files.
	 * @since 1.7.1
	 */
	Script readingFiles();

	/**
	 * With permission to write files.
	 * @since 1.7.1
	 */
	Script writingFiles();

	/**
	 * Without restrictions.
	 * @since 1.7.1
	 */
	Script withAllPermissions();

	/**
	 * With output stream.
	 * @since 1.7.1
	 */
	Script withOutput(PrintStream outStream);

	/**
	 * Execute on the given node.
	 * @since 1.7.1
	 */
	Object executeOn(NodeRO node);
}
