package org.freeplane.web;

/**
 * Returns the current program version.
 * 
 * @author robert ladstaetter
 */
public interface VersionClient {
	/**
	 * @return current version as string, null if not successful.
	 */
	public String getCurrentVersion();
}
