package org.freeplane.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import org.freeplane.core.util.LogTool;

/**
 * Handles communication with update webservice.
 * @author robert ladstaetter
 */
public class HttpVersionClient implements VersionClient {
	private String versionUrl;

	public HttpVersionClient(final String versionUrl) {
		setVersionUrl(versionUrl);
	}

	public String getCurrentVersion() {
		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new URL(versionUrl).openConnection().getInputStream()));
			final String version = in.readLine();
			return version;
		}
		catch (final MalformedURLException e) {
			LogTool.logException(e, "Url not well formed: " + versionUrl);
			return null;
		}
		catch (final IOException e) {
			LogTool.logException(e, "Could not read update url - check your internet connection.");
			return null;
		}
		finally {
			if (in != null) {
				try {
					in.close();
				}
				catch (final IOException e) {
					LogTool.warn("Couldn't close buffered reader.");
					return null;
				}
			}
		}
	}

	public void setVersionUrl(final String versionUrl) {
		this.versionUrl = versionUrl;
	}
}
